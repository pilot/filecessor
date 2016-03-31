package com.lazyants.filecessor.controller;

import com.lazyants.filecessor.configuration.ApplicationConfiguration;
import com.lazyants.filecessor.service.ImageHandler;
import com.lazyants.filecessor.utils.ExtensionGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
public class ResizeController {

    private final ApplicationConfiguration configuration;
    private final ImageHandler handler;

    @Autowired
    public ResizeController(ApplicationConfiguration configuration, ImageHandler handler) {
        this.configuration = configuration;
        this.handler = handler;
    }

    @RequestMapping("/crop_coordinates_{x1}x{y1}_{x2}x{y2}/{filename}.{ext}")
    public ResponseEntity<byte[]> coordinatesResize(@PathVariable int x1, @PathVariable int x2, @PathVariable int y1,
                                                    @PathVariable int y2, @PathVariable String filename, @PathVariable String ext) {
        BufferedImage image = handler.cropByCordinates(filename, ext, x1, y1, x2, y2);
        if (image == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Image not found");
        }

        return renderImage(image, ext);
    }

    @RequestMapping("/rotate_{degrees}/{filename}.{ext}")
    public ResponseEntity<byte[]> coordinatesResize(@PathVariable String filename, @PathVariable String ext, @PathVariable("degrees") int degrees) {
        BufferedImage image = handler.rotate(filename, ext, degrees);
        if (image == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Image not found");
        }

        return renderImage(image, ext);
    }

    private ResponseEntity<byte[]> renderImage(BufferedImage image, String ext) {

        try {
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(ExtensionGenerator.getMediaType(ext));

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write(image, ext, stream);
            stream.flush();
            byte[] result = stream.toByteArray();
            stream.close();
            return new ResponseEntity<>(result, headers, HttpStatus.OK);
        } catch (IOException ignore) {}

        return null;
    }
}
