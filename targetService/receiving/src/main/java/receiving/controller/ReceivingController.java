package receiving.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;


@RestController
class ReceivingController {

    private static final Logger logger = LoggerFactory.getLogger(receiving.controller.ReceivingController.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

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

}
