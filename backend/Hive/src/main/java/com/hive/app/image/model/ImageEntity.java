package com.hive.app.image.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public enum AssetType {
        PROFILE,
        EVENT,
    }


    public ImageEntity(AssetType assetType, Long parentId, String contentType, Long size, byte[] data) {
        this.assetType = assetType;
        this.parentId = parentId;
        this.contentType = contentType;
        this.size = size;
        this.data = data;
    }

}
