package rtsp.response.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;


@RestController
class ReceivingController {

    @Value("${file.directory}")
    private String fileDirectory;

    @Value("${file.name}")
    private String fileName;

    private Path filePath;

    private static final Logger logger = LoggerFactory.getLogger(rtsp.response.controller.ReceivingController.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    //Создание директории и файла для хранения результатов
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent() {
        filePath = Paths.get(fileDirectory, fileName);
        try {
            if (!Files.exists(Paths.get(fileDirectory))) {
                Files.createDirectories(Paths.get(fileDirectory));
                logger.info("Directory has been created: {}", fileDirectory);
            }
            Files.write(filePath, "".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            logger.info("File {} has been cleared", filePath.toString());
        } catch (IOException e) {
            logger.error("Error file cleanup", e);
        }
    }

    @PostMapping("/endpoint")
    public ResponseEntity<?> receiveFrame(@RequestBody Map<String, Object> payload) {
        try {
            //Извлечение данных из payload
            String encodedImage = (String) payload.get("image");
            Long timestamp = (Long) payload.get("timestamp");

            //Декодирование изображения из Base64
            BufferedImage image = decodeBase64Image(encodedImage);

            //Формирование ответа
            Map<String, Object> jsonResponse = new HashMap<>();
            jsonResponse.put("timestamp", timestamp);
            jsonResponse.put("imageWidth", image.getWidth());
            jsonResponse.put("imageHeight", image.getHeight());
            jsonResponse.put("imageType", image.getType());
            jsonResponse.put("message", "Frame processed successfully");

            logger.info("Frame processed successfully");

            String jsonString = objectMapper.writeValueAsString(jsonResponse);

            saveJsonResponseToFile(jsonString);

            return ResponseEntity.ok().body(jsonString);

        } catch (Exception e) {
            logger.error("Error frame processing", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error frame processing: " + e.getMessage());
        }
    }

    private BufferedImage decodeBase64Image(String encodedImage) throws IOException {
        byte[] imageBytes = Base64.getDecoder().decode(encodedImage);
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
            return ImageIO.read(bis);
        }
    }

    private void saveJsonResponseToFile(String jsonString) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(jsonString);
            writer.newLine();
        } catch (IOException e) {
            logger.error("Error adding JSON response to the file", e);
            throw e;
        }
    }
}
