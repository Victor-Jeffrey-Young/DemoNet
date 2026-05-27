package com.example.demonet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demonet.entity.Item;
import com.example.demonet.mapper.ItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataImportService implements CommandLineRunner {

    private final ItemMapper itemMapper;
    private final JdbcTemplate jdbcTemplate;

    private static final Map<String, List<String>> ITEM_TAGS = Map.ofEntries(
        Map.entry("hades-2", List.of("roguelike", "action", "mythology", "indie")),
        Map.entry("oppenheimer", List.of("biography", "drama", "history")),
        Map.entry("black-myth-wukong", List.of("action-rpg", "mythology", "soulslike")),
        Map.entry("dandadan", List.of("shonen", "supernatural", "comedy")),
        Map.entry("ark-nova", List.of("strategy", "hand-management", "tile-placement")),
        Map.entry("elden-ring-nightreign", List.of("soulslike", "coop", "dark-fantasy")),
        Map.entry("dune-part-two", List.of("scifi", "epic", "adaptation")),
        Map.entry("hollow-knight-silksong", List.of("metroidvania", "indie", "action")),
        Map.entry("frieren", List.of("fantasy", "drama", "slice-of-life")),
        Map.entry("brass-birmingham", List.of("strategy", "economic", "heavy")),
        Map.entry("metaphor-refantazio", List.of("jrpg", "fantasy", "turn-based")),
        Map.entry("chainsaw-man", List.of("shonen", "dark", "supernatural")),
        Map.entry("mgex-strike-freedom", List.of("gunpla", "mg", "seed")),
        Map.entry("terraforming-mars", List.of("strategy", "engine-building", "scifi")),
        Map.entry("the-boy-and-the-heron", List.of("ghibli", "fantasy", "oscar")),
        Map.entry("stalker-2", List.of("fps", "survival", "horror")),
        Map.entry("jujutsu-kaisen", List.of("shonen", "supernatural", "action")),
        Map.entry("pg-unleashed-rx78", List.of("gunpla", "pg", "classic")),
        Map.entry("super-mario-movie", List.of("animation", "comedy", "adaptation")),
        Map.entry("dune-imperium", List.of("strategy", "deck-building", "worker-placement")),
        Map.entry("gloomhaven", List.of("dungeon-crawl", "coop", "campaign", "heavy")),
        Map.entry("wingspan", List.of("engine-building", "family", "birds", "medium")),
        Map.entry("spiritisland", List.of("coop", "strategy", "complex", "area-control")),
        Map.entry("pandemic-legacy", List.of("coop", "campaign", "legacy", "medium")),
        Map.entry("catan", List.of("family", "trading", "gateway", "dice")),
        Map.entry("7-wonders-duel", List.of("two-player", "card-drafting", "civilization")),
        Map.entry("dune-imperium-uprising", List.of("strategy", "deck-building", "worker-placement")),
        Map.entry("lost-ruins-of-arnak", List.of("deck-building", "worker-placement", "exploration")),
        Map.entry("eclipse-second-dawn", List.of("4x", "scifi", "area-control", "heavy")),
        Map.entry("nemesis", List.of("survival", "horror", "traitor", "scifi")),
        Map.entry("wing-gundam-verka", List.of("gunpla", "mg", "verka", "wing")),
        Map.entry("sazabi-verka", List.of("gunpla", "mg", "verka", "zeon")),
        Map.entry("nu-gundam-verka", List.of("gunpla", "mg", "verka", "amuro")),
        Map.entry("unicorn-gundam-pg", List.of("gunpla", "pg", "unicorn", "psychoframe")),
        Map.entry("zoids-blade-liger", List.of("zoids", "hmm", "motorized")),
        Map.entry("evangelion-unit01-rg", List.of("evangelion", "rg", "kaworu")),
        Map.entry("barbatos-lupus-rex", List.of("gunpla", "mg", "ibo", "melee")),
        Map.entry("astray-red-frame-pg", List.of("gunpla", "pg", "astray", "katanas")),
        Map.entry("aerial-gundam-fm", List.of("gunpla", "fm", "witch", "gund-bit")),
        Map.entry("hi-nu-gundam-rg", List.of("gunpla", "rg", "char-counterattack")),
        Map.entry("three-body-problem", List.of("scifi", "chinese", "award-winning")),
        Map.entry("sapiens", List.of("history", "nonfiction", "anthropology")),
        Map.entry("neon-genesis-evangelion-manga", List.of("manga", "mecha", "psychological"))
    );

    @Override
    @Transactional
    public void run(String... args) {
        if (itemMapper.selectCount(new LambdaQueryWrapper<>()) > 0) {
            log.info("Data already exists, skipping seed import.");
            return;
        }

        log.info("Importing seed data...");

        insert("game", "Hades II", "hades-2",
                "Supergiant 最新力作，冥界公主的复仇之旅。暗黑神话 Rogue-like，每场战斗都是不一样的体验。",
                "{\"developer\":\"Supergiant Games\",\"genre\":\"Roguelike\",\"platform\":\"PC,PS5,Xbox\"}");
        insert("movie", "Oppenheimer", "oppenheimer",
                "诺兰执导原子弹之父传记片。IMAX 胶片拍摄的视觉奇观，探讨科学与道德。",
                "{\"director\":\"Christopher Nolan\",\"year\":2023,\"duration\":\"180min\"}");
        insert("game", "Black Myth: Wukong", "black-myth-wukong",
                "游戏科学工作室开发的国产 ARPG 大作，基于西游记改编。",
                "{\"developer\":\"游戏科学\",\"genre\":\"ARPG\",\"platform\":\"PC,PS5\"}");
        insert("anime", "Dandadan", "dandadan",
                "龙幸伸的超自然战斗喜剧漫画改编，幽灵 vs 外星人。",
                "{\"studio\":\"Science SARU\",\"episodes\":12,\"year\":2024}");
        insert("boardgame", "Ark Nova", "ark-nova",
                "打造一个成功的动物园！手牌管理与板块放置的完美结合。BGG 排名前 5。",
                "{\"players\":\"1-4\",\"playtime\":\"90-150min\",\"weight\":3.7}");
        insert("game", "Elden Ring: Nightreign", "elden-ring-nightreign",
                "FromSoftware 全新合作生存玩法。黑夜降临，三人协力挑战三天循环的生存战。",
                "{\"developer\":\"FromSoftware\",\"genre\":\"Survival RPG\",\"platform\":\"PC,PS5,Xbox\"}");
        insert("movie", "Dune: Part Two", "dune-part-two",
                "维伦纽瓦史诗续作。保罗·厄崔迪与弗雷曼人联合，展开对银河的复仇之战。",
                "{\"director\":\"Denis Villeneuve\",\"year\":2024,\"duration\":\"166min\"}");
        insert("game", "Hollow Knight: Silksong", "hollow-knight-silksong",
                "Team Cherry 精雕细琢的续作。黄蜂女踏上丝歌之旅，全新的敌人与王国等待探索。",
                "{\"developer\":\"Team Cherry\",\"genre\":\"Metroidvania\",\"platform\":\"PC,Switch\"}");
        insert("anime", "Frieren: Beyond Journey's End", "frieren",
                "葬送的芙莉莲。长寿精灵魔法师在旅途中重新理解人类生命的短暂与珍贵。",
                "{\"studio\":\"MADHOUSE\",\"episodes\":28,\"year\":2023}");
        insert("boardgame", "Brass: Birmingham", "brass-birmingham",
                "工业革命主题的策略桌游。在运河与铁路时代中建造工厂，BGG 排名第1。",
                "{\"players\":\"2-4\",\"playtime\":\"120-180min\",\"weight\":3.9}");
        insert("game", "Metaphor: ReFantazio", "metaphor-refantazio",
                "Atlus 全新奇幻 RPG。在选举中周游王国，唤醒沉睡的魔法机甲拯救世界。",
                "{\"developer\":\"Atlus\",\"genre\":\"JRPG\",\"platform\":\"PC,PS5,Xbox\"}");
        insert("anime", "Chainsaw Man", "chainsaw-man",
                "藤本树异色少年漫画改编。为还债与恶魔签订契约的少年，电锯轰鸣斩碎一切。",
                "{\"studio\":\"MAPPA\",\"episodes\":12,\"year\":2022}");
        insert("model", "MGEX Strike Freedom Gundam", "mgex-strike-freedom",
                "万代 MGEX 系列旗舰产品。金色骨架多色电镀，极致可动与细节表现。",
                "{\"grade\":\"MGEX\",\"scale\":\"1/100\",\"material\":\"PS,ABS,PE\",\"series\":\"SEED\"}");
        insert("boardgame", "Terraforming Mars", "terraforming-mars",
                "火星改造计划。通过科技、资源建设将红色星球变为人类新家园。",
                "{\"players\":\"1-5\",\"playtime\":\"120min\",\"weight\":3.2}");
        insert("movie", "The Boy and the Heron", "the-boy-and-the-heron",
                "宫崎骏 10 年后回归之作。少年与苍鹭的奇幻冒险，奥斯卡最佳动画长片。",
                "{\"director\":\"宫崎骏\",\"year\":2023,\"duration\":\"124min\"}");
        insert("game", "S.T.A.L.K.E.R. 2", "stalker-2",
                "GSC Game World 开发的硬核生存 FPS。重返切尔诺贝利禁区的心跳之旅。",
                "{\"developer\":\"GSC Game World\",\"genre\":\"Survival FPS\",\"platform\":\"PC,Xbox\"}");
        insert("anime", "Jujutsu Kaisen", "jujutsu-kaisen",
                "咒术回战。咒术师与诅咒之战，MAPPA 制作的顶级战斗动画。",
                "{\"studio\":\"MAPPA\",\"episodes\":48,\"year\":2020}");
        insert("model", "PG Unleashed RX-78-2 Gundam", "pg-unleashed-rx78",
                "完美级释放版元祖高达。三阶段组合结构，内部机械细节达到全新高度。",
                "{\"grade\":\"PGU\",\"scale\":\"1/60\",\"material\":\"PS,ABS,PE\",\"series\":\"UC\"}");
        insert("movie", "The Super Mario Bros. Movie", "super-mario-movie",
                "照明娱乐制作的马力欧大电影。水管工兄弟的蘑菇王国冒险，全球票房破10亿。",
                "{\"director\":\"Aaron Horvath\",\"year\":2023,\"duration\":\"92min\"}");
        insert("boardgame", "Dune: Imperium", "dune-imperium",
                "沙丘帝国。工人放置+牌库构筑的完美融合，体验阿拉基斯的权谋与战争。",
                "{\"players\":\"1-4\",\"playtime\":\"60-120min\",\"weight\":3.0}");

        insert("boardgame", "Gloomhaven", "gloomhaven",
                "史诗级地牢探索桌游。宏大世界观、永久变化的战役系统，BGG 长期霸主。",
                "{\"players\":\"1-4\",\"playtime\":\"60-120min\",\"weight\":3.9}");
        insert("boardgame", "Wingspan", "wingspan",
                "鸟类主题引擎构筑游戏。精美插画、流畅节奏，吸引了大量轻度玩家入坑。",
                "{\"players\":\"1-5\",\"playtime\":\"40-70min\",\"weight\":2.4}");
        insert("boardgame", "Spirit Island", "spiritisland",
                "合作守护岛屿的深度策略桌游。扮演自然精灵击退殖民者，反向塔防。",
                "{\"players\":\"1-4\",\"playtime\":\"90-120min\",\"weight\":4.0}");
        insert("boardgame", "Pandemic Legacy: Season 1", "pandemic-legacy",
                "变革桌游业界的传承游戏。病毒蔓延全球，每一次选择都永久改变游戏。",
                "{\"players\":\"2-4\",\"playtime\":\"60min\",\"weight\":2.8}");
        insert("boardgame", "Catan", "catan",
                "经典入门桌游。收集资源、拓展领地、进行贸易，现代桌游复兴的开端。",
                "{\"players\":\"3-4\",\"playtime\":\"60-120min\",\"weight\":2.3}");
        insert("boardgame", "7 Wonders Duel", "7-wonders-duel",
                "双人文明建设对决。三线推拉、奇迹争夺，最佳二人桌游之一。",
                "{\"players\":\"2\",\"playtime\":\"30min\",\"weight\":2.2}");
        insert("boardgame", "Dune: Imperium - Uprising", "dune-imperium-uprising",
                "沙丘帝国独立扩展。新增间谍、沙虫战斗，策略维度大幅提升。",
                "{\"players\":\"1-4\",\"playtime\":\"60-120min\",\"weight\":3.3}");
        insert("boardgame", "Lost Ruins of Arnak", "lost-ruins-of-arnak",
                "考古探索主题。牌库构筑与工人放置的优雅结合，探索失落的神庙。",
                "{\"players\":\"1-4\",\"playtime\":\"60-120min\",\"weight\":2.9}");
        insert("boardgame", "Eclipse: Second Dawn", "eclipse-second-dawn",
                "宏伟的 4X 太空史诗。六大种族争霸银河，科技研发与舰队对决。",
                "{\"players\":\"2-6\",\"playtime\":\"120-240min\",\"weight\":3.7}");
        insert("boardgame", "Nemesis", "nemesis",
                "异形主题半合作生存桌游。飞船暗藏异形，谁能活着回到地球？",
                "{\"players\":\"1-5\",\"playtime\":\"90-180min\",\"weight\":3.4}");

        insert("model", "MG Wing Gundam Zero EW Ver.Ka", "wing-gundam-verka",
                "零式飞翼高达卡版。天使之翼展开超过 50cm，精密内构与珍珠白涂装。",
                "{\"grade\":\"MG\",\"scale\":\"1/100\",\"material\":\"PS,ABS\",\"series\":\"Wing EW\"}");
        insert("model", "MG Sazabi Ver.Ka", "sazabi-verka",
                "夏亚座驾沙扎比卡版。机体质感重磅，水贴纸极尽华丽，MG 巅峰之作。",
                "{\"grade\":\"MG\",\"scale\":\"1/100\",\"material\":\"PS,ABS,PE\",\"series\":\"Char's Counterattack\"}");
        insert("model", "MG Nu Gundam Ver.Ka", "nu-gundam-verka",
                "阿姆罗座驾 ν 高达卡版。精神力框架采用绿色透明件，分件细致。",
                "{\"grade\":\"MG\",\"scale\":\"1/100\",\"material\":\"PS,ABS,PP\",\"series\":\"Char's Counterattack\"}");
        insert("model", "PG Unicorn Gundam", "unicorn-gundam-pg",
                "独角兽高达完美级。可切换独角模式与毁灭模式，精神骨架采用 LED 发光。",
                "{\"grade\":\"PG\",\"scale\":\"1/60\",\"material\":\"PS,ABS,LED\",\"series\":\"UC\"}");
        insert("model", "HMM Zoids Blade Liger", "zoids-blade-liger",
                "寿屋 HMM 系列剑刃狮。高机动索斯兽，利爪与激光刃可动，气势非凡。",
                "{\"grade\":\"HMM\",\"scale\":\"1/72\",\"material\":\"PS,PE,ABS\",\"series\":\"Zoids\"}");
        insert("model", "RG Evangelion Unit-01", "evangelion-unit01-rg",
                "EVA 初号机 RG 版。肌肉纤维纹理外甲，真实系机械结构再现。",
                "{\"grade\":\"RG\",\"scale\":\"1/144\",\"material\":\"PS,ABS\",\"series\":\"Evangelion\"}");
        insert("model", "MG Barbatos Lupus Rex", "barbatos-lupus-rex",
                "铁血孤儿最终决战形态。巨大狼爪与尾巴钢鞭，狂战士般的站姿。",
                "{\"grade\":\"MG\",\"scale\":\"1/100\",\"material\":\"PS,ABS\",\"series\":\"IBO\"}");
        insert("model", "PG Astray Red Frame", "astray-red-frame-pg",
                "异端红色机完美级。标志性双太刀菊一文字，骨架全展示结构。",
                "{\"grade\":\"PG\",\"scale\":\"1/60\",\"material\":\"PS,ABS,PE\",\"series\":\"SEED Astray\"}");
        insert("model", "FM Aerial Gundam", "aerial-gundam-fm",
                "水星魔女主角机风灵高达。透明位元浮游炮可独立展示，Full Mechanics 精良分件。",
                "{\"grade\":\"FM\",\"scale\":\"1/100\",\"material\":\"PS,PE\",\"series\":\"Witch from Mercury\"}");
        insert("model", "RG Hi-Nu Gundam", "hi-nu-gundam-rg",
                "海牛高达 RG 版。银蓝配色加浮游炮展开，RG 系列最高杰作之一。",
                "{\"grade\":\"RG\",\"scale\":\"1/144\",\"material\":\"PS,ABS,PP\",\"series\":\"Char's Counterattack MSV\"}");

        insert("book", "三体", "three-body-problem",
                "刘慈欣雨果奖获奖科幻巨作。文化大革命中人类向宇宙发出第一声啼鸣，引发星际文明的三体危机。",
                "{\"author\":\"刘慈欣\",\"pages\":312,\"year\":2008,\"category\":\"科幻\"}");
        insert("book", "人类简史", "sapiens",
                "尤瓦尔·赫拉利的人类史诗。从认知革命到科学革命，讲述智人如何征服世界。",
                "{\"author\":\"Yuval Noah Harari\",\"pages\":464,\"year\":2014,\"category\":\"历史\"}");
        insert("book", "新世纪福音战士", "neon-genesis-evangelion-manga",
                "贞本义行执笔的经典漫画。少年驾驶 EVA 对抗使徒，心理剖析远超普通机械动画。",
                "{\"author\":\"贞本义行\",\"pages\":1800,\"year\":1994,\"category\":\"漫画\"}");
        insert("book", "沙丘", "dune-book",
                "弗兰克·赫伯特的科幻史诗。香料、沙漠行星、预知能力，沙丘宇宙的宏伟开篇。",
                "{\"author\":\"Frank Herbert\",\"pages\":688,\"year\":1965,\"category\":\"科幻\"}");

        insert("music", "Interstellar Soundtrack", "interstellar-ost",
                "Hans Zimmer 为诺兰《星际穿越》打造的史诗配乐。管风琴与弦乐的磅礴交织，穿越维度。",
                "{\"artist\":\"Hans Zimmer\",\"year\":2014,\"genre\":\"电影原声\",\"tracks\":16}");
        insert("music", "Random Access Memories", "random-access-memories",
                "Daft Punk 格莱美年度专辑。复古与未来交织的电子音乐里程碑。",
                "{\"artist\":\"Daft Punk\",\"year\":2013,\"genre\":\"电子\",\"tracks\":13}");
        insert("music", "Uta no Prince-sama", "uta-no-prince",
                "歌之王子殿下。Julian 与一众声优带来的梦幻音乐盛宴。",
                "{\"artist\":\"Various\",\"year\":2011,\"genre\":\"J-POP\",\"tracks\":12}");

        insert("digital", "Sony WH-1000XM5", "sony-wh1000xm5",
                "Sony 旗舰降噪耳机。自适应降噪、30 小时续航，通勤与专注的终极伴侣。",
                "{\"brand\":\"Sony\",\"category\":\"耳机\",\"year\":2022,\"features\":\"主动降噪,蓝牙5.2,30h续航\"}");
        insert("digital", "Apple AirPods Pro 2", "airpods-pro-2",
                "Apple H2 芯片加持，空间音频与自适应通透模式。降噪提升 2 倍。",
                "{\"brand\":\"Apple\",\"category\":\"耳机\",\"year\":2022,\"features\":\"空间音频,自适应降噪,MagSafe\"}");
        insert("digital", "Cherry MX Board 3.0S", "cherry-mx-board",
                "Cherry MX 红轴，无钢板设计回归纯粹机械手感。办公游戏兼备。",
                "{\"brand\":\"Cherry\",\"category\":\"键盘\",\"year\":2021,\"features\":\"MX红轴,RGB背光,109键\"}");

        seedTags();

        log.info("Seed data imported: {} items, {} tags",
                itemMapper.selectCount(null),
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tags", Long.class));
    }

    private void seedTags() {
        for (var entry : ITEM_TAGS.entrySet()) {
            List<Long> ids = jdbcTemplate.queryForList(
                    "SELECT id FROM items WHERE slug = ?", Long.class, entry.getKey());
            if (ids.isEmpty()) continue;
            Long itemId = ids.get(0);

            for (String tagName : entry.getValue()) {
                jdbcTemplate.update("INSERT IGNORE INTO tags (name) VALUES (?)", tagName);
                Long tagId = jdbcTemplate.queryForObject(
                        "SELECT id FROM tags WHERE name = ?", Long.class, tagName);
                if (tagId != null) {
                    jdbcTemplate.update(
                            "INSERT IGNORE INTO item_tag_mapping (item_id, tag_id) VALUES (?, ?)",
                            itemId, tagId);
                }
            }
        }
    }

    private void insert(String type, String title, String slug, String description, String infoJson) {
        Item item = new Item();
        item.setType(type);
        item.setTitle(title);
        item.setSlug(slug);
        item.setDescription(description);
        item.setInfoJson(infoJson);
        item.setSource("manual");
        item.setStatus(1);
        itemMapper.insert(item);
    }
}
