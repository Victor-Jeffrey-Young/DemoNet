DELETE FROM items;

INSERT INTO items (type, title, slug, cover_url, description, source, status) VALUES
('game', 'Hades II', 'hades-2', '', 'Supergiant 最新力作，冥界公主的复仇之旅。暗黑神话 Rogue-like，每场战斗都是不一样的体验。', 'manual', 1),
('movie', 'Oppenheimer', 'oppenheimer', '', '诺兰执导原子弹之父传记片。IMAX 胶片拍摄的视觉奇观，探讨科学与道德。', 'manual', 1),
('game', 'Black Myth: Wukong', 'black-myth-wukong', '', '游戏科学工作室开发的国产 ARPG 大作，基于西游记改编。', 'manual', 1),
('anime', 'Dandadan', 'dandadan', '', '龙幸伸的超自然战斗喜剧漫画改编，幽灵 vs 外星人。', 'manual', 1),
('boardgame', 'Ark Nova', 'ark-nova', '', '打造一个成功的动物园！手牌管理与板块放置的完美结合。BGG 排名前 5。', 'manual', 1);
