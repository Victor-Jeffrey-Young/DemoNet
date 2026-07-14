CREATE TABLE curation_collections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    slug VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(500) NOT NULL,
    cover_url VARCHAR(512),
    constraints_json JSON,
    visible TINYINT NOT NULL DEFAULT 0,
    display_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_curation_collections_visible_order (visible, display_order)
);

CREATE TABLE curation_collection_items (
    collection_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    reason VARCHAR(300) NOT NULL,
    editor_note TEXT,
    PRIMARY KEY (collection_id, item_id),
    INDEX idx_curation_collection_items_item (item_id),
    CONSTRAINT fk_curation_collection_items_collection
        FOREIGN KEY (collection_id) REFERENCES curation_collections(id) ON DELETE CASCADE,
    CONSTRAINT fk_curation_collection_items_item
        FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);
