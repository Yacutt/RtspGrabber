package rtsp.processing.service;

import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessingService.class);

    @Value("${rtsp.url}")
    private String rtspUrl;

    @Value("${target.service.url}")
    private String targetServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void processStream() {
        FFmpegFrameGrabber grabber = null;
        try {
            grabber = new FFmpegFrameGrabber(rtspUrl);
            grabber.setFormat("rtsp");
            grabber.setOption("rtsp_transport", "tcp");
            FFmpegLogCallback.set();
            grabber.start();
            while (true) {
                Frame frame = grabber.grabImage();
                if (frame == null) {
                    logger.warn("The frame is empty. Reconnection attempt");
                    grabber.stop();
                    grabber.start();
                    continue;
                }
                //Преобразование кадра
                BufferedImage bufferedImage = convertFrameToBufferedImage(frame);

                //Кодирование в Base64
                String encodedImage = encodeBufferedImageToBase64(bufferedImage);

                //Получение текущей временной метки
                long timestamp = Instant.now().toEpochMilli();

                sendFrameToTargetService(encodedImage, timestamp);
            }

        } catch (FrameGrabber.Exception e) {
            logger.error("Error when capturing a frame from an RTSP stream", e);
        } catch (IOException e) {
            logger.error("Error Input/Output", e);
        } finally {
            try {
                if (grabber != null) {
                    grabber.stop();
                    grabber.release();
                }
                logger.info("Grabber is stopped and released");
            } catch (IOException e) {
                logger.error("Error when stopping or releasing grabber", e);
            }
        }

        logger.warn("Processing of the RTSP stream has been stopped");
    }


    private BufferedImage convertFrameToBufferedImage(Frame frame) {
        Java2DFrameConverter converter = new Java2DFrameConverter();
        return converter.getBufferedImage(frame);
    }

    private String encodeBufferedImageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }


    private void sendFrameToTargetService(String encodedImage, long timestamp) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("image", encodedImage);
            requestBody.put("timestamp", timestamp);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            String response = restTemplate.postForObject(targetServiceUrl, request, String.class);
            logger.info("The frame has been sent. Target service's response: {}", response);

        } catch (Exception e) {
            logger.error("Error when sending a frame to the target service", e);
        }
    }
}
