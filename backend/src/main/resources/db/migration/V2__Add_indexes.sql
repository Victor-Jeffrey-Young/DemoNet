CREATE INDEX idx_reviews_item ON reviews (item_id);
CREATE INDEX idx_reviews_user ON reviews (user_id);
CREATE UNIQUE INDEX idx_user_item ON user_items (user_id, item_id);
CREATE INDEX idx_items_status ON items (status);
CREATE INDEX idx_itm_tag ON item_tag_mapping (tag_id);
