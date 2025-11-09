package com.hive.app.image.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * if AssetType:PROFILE parentId is UserId
 * if AssetType:EVENT parentId is EventId
 */
@Entity
@Table(name = "IMAGES", indexes = {
        @Index(name = "IMAGE_OWNER_IDX", columnList = "ASSET_TYPE, PARENT_ID")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "PARENT_ID", nullable = false)
    private Long parentId;
    @Column(name = "ASSET_TYPE", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private AssetType assetType;
    @Column(nullable = false)
    private String contentType;
    @Column(nullable = false)
    private Long size;
    @Lob
    @Column(nullable = false)
    private byte[] data;

    public enum AssetType {PROFILE, EVENT, BG}

    @Component
    public static class StringToAssetTypeConverter implements Converter<String, AssetType> {

        @Override
        public AssetType convert(String source) {
            try {
                return AssetType.valueOf(source.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new ConversionFailedException(
                        TypeDescriptor.valueOf(String.class),
                        TypeDescriptor.valueOf(AssetType.class),
                        source,
                        ex);
            }
        }
    }
}
