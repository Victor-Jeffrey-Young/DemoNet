SET NAMES utf8mb4;
UPDATE items SET info_json = JSON_SET(info_json, '$.dlc', '[{"id":4586820,"name":"《守望先锋®》——终极战令礼包2026：第3赛季"},{"id":4586830,"name":"《守望先锋®》开战礼包2026：第3赛季"},{"id":4586840,"name":"《守望先锋》：半藏完整神话武器皮肤礼包"},{"id":2437280,"name":"《守望先锋®》-  入侵礼包"}]') WHERE id = 577;
