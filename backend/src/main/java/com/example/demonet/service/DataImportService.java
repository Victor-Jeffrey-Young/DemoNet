package com.example.demonet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demonet.entity.Item;
import com.example.demonet.mapper.ItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

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

    private static final List<String> ALL_TAGS = ITEM_TAGS.values().stream()
            .flatMap(List::stream).distinct().sorted().toList();

    @Override
    @Transactional
    public void run(String... args) {
        seedUser();

        if (itemMapper.selectCount(new LambdaQueryWrapper<>()) > 0) {
            log.info("Data already exists, skipping seed import.");
            seedAnimeIfMissing();
            return;
        }

        log.info("Importing seed data...");

        insert("game", "Hades II", "hades-2",
                "Supergiant 最新力作，冥界公主的复仇之旅。暗黑神话 Rogue-like，每场战斗都是不一样的体验。",
                "{\"developer\":\"Supergiant Games\",\"genre\":\"Roguelike\",\"platform\":\"PC,PS5,Xbox\",\"demo_available\":true,\"demo_url\":\"steam://install/2846690\",\"videos\":{\"youtube\":\"https://www.youtube.com/embed/TUOERgENrVs\",\"bilibili\":\"//player.bilibili.com/player.html?bvid=BV1Wx4y1W7Gh\"}}",
                "https://picsum.photos/seed/hades2/600/400", "https://store.steampowered.com/app/1145350/", null);
        insert("movie", "奥本海默", "oppenheimer",
                "克里斯托弗·诺兰执导的原子弹之父传记片。IMAX 胶片拍摄的视觉奇观，探讨科学与道德。",
                "{\"director\":\"Christopher Nolan\",\"year\":2023,\"duration\":\"180min\",\"genre\":\"剧情, 历史, 惊悚\",\"trailer\":\"https://www.youtube.com/embed/uYPbbksJxIg\",\"videos\":{\"youtube\":\"https://www.youtube.com/embed/uYPbbksJxIg\",\"bilibili\":\"\"}}",
                "https://image.tmdb.org/t/p/w500/a6v21Mgz2w6OQL7ezkQxGbGA92W.jpg", "https://www.themoviedb.org/movie/872585", null);
        insert("game", "Black Myth: Wukong", "black-myth-wukong",
                "游戏科学工作室开发的国产 ARPG 大作，基于西游记改编。",
                "{\"developer\":\"游戏科学\",\"genre\":\"ARPG\",\"platform\":\"PC,PS5\",\"demo_available\":true}",
                "https://picsum.photos/seed/wukong/600/400", "https://store.steampowered.com/app/2358720/",
                "steam://install/2581550");
        insert("movie", "Dune: Part Two", "dune-part-two",
                "维伦纽瓦史诗续作。保罗·厄崔迪与弗雷曼人联合，展开对银河的复仇之战。",
                "{\"director\":\"Denis Villeneuve\",\"year\":2024,\"duration\":\"166min\"}",
                "https://picsum.photos/seed/dune2/600/400", null, null);
        insert("game", "Elden Ring: Nightreign", "elden-ring-nightreign",
                "FromSoftware 全新合作生存玩法。黑夜降临，三人协力挑战三天循环的生存战。",
                "{\"developer\":\"FromSoftware\",\"genre\":\"Survival RPG\",\"platform\":\"PC,PS5,Xbox\",\"demo_available\":false}",
                "https://picsum.photos/seed/elden/600/400", "https://store.steampowered.com/app/1245620/", null);
        insert("game", "Hollow Knight: Silksong", "hollow-knight-silksong",
                "Team Cherry 精雕细琢的续作。黄蜂女踏上丝歌之旅，全新的敌人与王国等待探索。",
                "{\"developer\":\"Team Cherry\",\"genre\":\"Metroidvania\",\"platform\":\"PC,Switch\",\"demo_available\":false}",
                "https://picsum.photos/seed/silksong/600/400", null, null);
        insert("game", "Metaphor: ReFantazio", "metaphor-refantazio",
                "Atlus 全新奇幻 RPG。在选举中周游王国，唤醒沉睡的魔法机甲拯救世界。",
                "{\"developer\":\"Atlus\",\"genre\":\"JRPG\",\"platform\":\"PC,PS5,Xbox\",\"demo_available\":true}",
                "https://picsum.photos/seed/metaphor/600/400", "https://store.steampowered.com/app/2679460/",
                "steam://install/3130330");
        insert("game", "S.T.A.L.K.E.R. 2", "stalker-2",
                "GSC Game World 开发的硬核生存 FPS。重返切尔诺贝利禁区的心跳之旅。",
                "{\"developer\":\"GSC Game World\",\"genre\":\"Survival FPS\",\"platform\":\"PC,Xbox\",\"demo_available\":false}",
                "https://picsum.photos/seed/stalker2/600/400", "https://store.steampowered.com/app/1643320/", null);

        insert("game", "Celeste", "celeste",
                "像素平台跳跃神作。帮助 Madeline 攀登塞莱斯特山，超过 700 个关卡 + 内置开发者评论。",
                "{\"developer\":\"Extremely OK Games\",\"genre\":\"Platformer\",\"platform\":\"PC,Switch,PS4\",\"demo_available\":true}",
                "https://picsum.photos/seed/celeste/600/400", "https://store.steampowered.com/app/504230/",
                "https://mattmakesgames.itch.io/celeste-classic");
        insert("game", "Buckshot Roulette", "buckshot-roulette",
                "俄罗斯轮盘赌 + 霰弹枪。与庄家对赌，道具有限、运气与胆量的博弈。itch 爆款独立游戏。",
                "{\"developer\":\"Mike Klubnika\",\"genre\":\"Table Horror\",\"platform\":\"PC\",\"demo_available\":true}",
                "https://picsum.photos/seed/buckshot/600/400", "https://store.steampowered.com/app/2835570/",
                "https://mikeklubnika.itch.io/buckshot-roulette");

        insert("movie", "The Boy and the Heron", "the-boy-and-the-heron",
                "宫崎骏 10 年后回归之作。少年与苍鹭的奇幻冒险，奥斯卡最佳动画长片。",
                "{\"director\":\"宫崎骏\",\"year\":2023,\"duration\":\"124min\"}",
                "https://picsum.photos/seed/heron/600/400", null);
        insert("movie", "The Super Mario Bros. Movie", "super-mario-movie",
                "照明娱乐制作的马力欧大电影。水管工兄弟的蘑菇王国冒险，全球票房破10亿。",
                "{\"director\":\"Aaron Horvath\",\"year\":2023,\"duration\":\"92min\"}",
                "https://picsum.photos/seed/mario/600/400", null);
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
                "{\"author\":\"刘慈欣\",\"pages\":312,\"year\":2008,\"category\":\"科幻\",\"reader_url\":\"\"}");
        insert("book", "人类简史", "sapiens",
                "尤瓦尔·赫拉利的人类史诗。从认知革命到科学革命，讲述智人如何征服世界。",
                "{\"author\":\"Yuval Noah Harari\",\"pages\":464,\"year\":2014,\"category\":\"历史\"}");
        insert("book", "新世纪福音战士", "neon-genesis-evangelion-manga",
                "贞本义行执笔的经典漫画。少年驾驶 EVA 对抗使徒，心理剖析远超普通机械动画。",
                "{\"author\":\"贞本义行\",\"pages\":1800,\"year\":1994,\"category\":\"漫画\"}");
        insert("book", "沙丘", "dune-book",
                "弗兰克·赫伯特的科幻史诗。香料、沙漠行星、预知能力，沙丘宇宙的宏伟开篇。",
                "{\"author\":\"Frank Herbert\",\"pages\":688,\"year\":1965,\"category\":\"科幻\"}");
        insert("book", "活着", "huozhe",
                "余华代表作。地主少爷福贵的一生，历经内战、土改、文革，讲述人对苦难的承受能力。",
                "{\"author\":\"余华\",\"pages\":192,\"year\":1992,\"category\":\"文学\"}");
        insert("book", "百年孤独", "cien-anos-de-soledad",
                "马尔克斯魔幻现实主义巅峰。布恩迪亚家族七代兴衰，马孔多的雨下了四年。",
                "{\"author\":\"Gabriel García Márquez\",\"pages\":360,\"year\":1967,\"category\":\"文学\"}");
        insert("book", "1984", "nineteen-eighty-four",
                "乔治·奥威尔的反乌托邦经典。老大哥在看着你。思想警察、新话、双重思想。",
                "{\"author\":\"George Orwell\",\"pages\":328,\"year\":1949,\"category\":\"反乌托邦\"}");
        insert("book", "挪威的森林", "norwegian-wood",
                "村上春树青春小说。渡边在直子与绿子之间的青春迷惘，披头士旋律贯穿始终。",
                "{\"author\":\"村上春树\",\"pages\":384,\"year\":1987,\"category\":\"文学\"}");
        insert("book", "银河帝国：基地", "foundation",
                "阿西莫夫科幻史诗。心理史学预言银河帝国覆灭，基地计划为文明保存火种。",
                "{\"author\":\"Isaac Asimov\",\"pages\":296,\"year\":1951,\"category\":\"科幻\"}");
        insert("book", "小王子", "le-petit-prince",
                "圣-埃克苏佩里童话。来自B-612星球的小王子游历宇宙，驯服与爱的永恒寓言。",
                "{\"author\":\"Antoine de Saint-Exupéry\",\"pages\":96,\"year\":1943,\"category\":\"童话\",\"reader_url\":\"\"}");

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
        jdbcTemplate.update("INSERT IGNORE INTO tags (name) VALUES " + String.join(",", ALL_TAGS.stream().map(t -> "('" + t + "')").toList()));
    }

    private void seedAnimeIfMissing() {
        Long count = itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getType, "anime"));
        if (count != null && count > 0) return;
        log.info("Anime data missing, seeding...");
        i("anime", "葬送的芙莉莲", "sousou-no-frieren",
                "勇者一行击败魔王后，精灵魔法使芙莉莲踏上理解人类情感的旅程。2023 年度动画天花板，温柔而深沉。",
                "{\"studio\":\"MADHOUSE\",\"year\":2023,\"genre\":\"奇幻, 治愈\",\"origin\":\"日漫\",\"videos\":{\"bilibili\":\"//player.bilibili.com/player.html?bvid=BV1uN411n7zt\"}}",
                "https://picsum.photos/seed/frieren/600/400", "//player.bilibili.com/player.html?bvid=BV1uN411n7zt");
        i("anime", "鬼灭之刃 柱训练篇", "kimetsu-no-yaiba-hashira",
                "炭治郎一行参加鬼杀队柱级训练，为最终决战做准备。飞碟社顶级作画，水之呼吸视觉盛宴。",
                "{\"studio\":\"ufotable\",\"year\":2024,\"genre\":\"热血, 战斗\",\"origin\":\"日漫\",\"videos\":{\"bilibili\":\"//player.bilibili.com/player.html?bvid=BV1Ff421m7TF\"}}",
                "https://picsum.photos/seed/kimetsu/600/400", "//player.bilibili.com/player.html?bvid=BV1Ff421m7TF");
        i("anime", "咒术回战 第二季", "jujutsu-kaisen-s2",
                "涩谷事变篇。五条悟被封印，虎杖一行人面对前所未有的危机。MAPPA 巅峰作画水准。",
                "{\"studio\":\"MAPPA\",\"year\":2023,\"genre\":\"战斗, 黑暗奇幻\",\"origin\":\"日漫\",\"videos\":{\"bilibili\":\"//player.bilibili.com/player.html?bvid=BV1tG41167bs\"}}",
                "https://picsum.photos/seed/jujutsu/600/400", "//player.bilibili.com/player.html?bvid=BV1tG41167bs");
        i("anime", "迷宫饭", "dungeon-meshi",
                "在迷宫中把魔物做成美食！冒险者小队没钱吃饭？那就把史莱姆炖了。Trigger 出品的美食冒险喜剧。",
                "{\"studio\":\"Trigger\",\"year\":2024,\"genre\":\"美食, 冒险, 喜剧\",\"origin\":\"日漫\",\"videos\":{\"bilibili\":\"//player.bilibili.com/player.html?bvid=BV1HC411h7RZ\"}}",
                "https://picsum.photos/seed/dungeon-meshi/600/400", "//player.bilibili.com/player.html?bvid=BV1HC411h7RZ");
        i("anime", "药屋少女的呢喃", "kusuriya-no-hitorigoto",
                "后宫试毒少女猫猫靠药理知识破解宫廷谜案。中国风宫廷悬疑 + 独特女主魅力。",
                "{\"studio\":\"TOHO animation STUDIO×OLM\",\"year\":2023,\"genre\":\"悬疑, 宫廷, 历史\",\"origin\":\"日漫\",\"videos\":{\"bilibili\":\"//player.bilibili.com/player.html?bvid=BV1xH4y1y7HK\"}}",
                "https://picsum.photos/seed/kusuriya/600/400", "//player.bilibili.com/player.html?bvid=BV1xH4y1y7HK");
        i("anime", "【我推的孩子】", "oshi-no-ko",
                "偶像×转生×娱乐圈悬疑。星野爱被刺杀后，她的双胞胎踏上真相之路。第一集 90 分钟封神。",
                "{\"studio\":\"动画工房\",\"year\":2023,\"genre\":\"悬疑, 偶像, 剧情\",\"origin\":\"日漫\",\"videos\":{\"bilibili\":\"//player.bilibili.com/player.html?bvid=BV1Bs4y1X7gS\"}}",
                "https://picsum.photos/seed/oshinoko/600/400", "//player.bilibili.com/player.html?bvid=BV1Bs4y1X7gS");
        i("anime", "中国奇谭", "chinese-folktales",
                "上美影 × B站联合出品。8 个中国妖怪故事，水墨剪纸黏土定格多元风格，浪浪山小猪妖刷屏全网。",
                "{\"studio\":\"上海美术电影制片厂\",\"year\":2023,\"genre\":\"奇幻, 中国风, 短片集\",\"origin\":\"国漫\",\"videos\":{\"bilibili\":\"//player.bilibili.com/player.html?bvid=BV1t24y1r7dN\"}}",
                "https://picsum.photos/seed/folktales/600/400", "//player.bilibili.com/player.html?bvid=BV1t24y1r7dN");
        i("anime", "雾山五行", "wushan-wuxing",
                "林魂导演的水墨战斗神作。五行家族与妖兽的宿命对决，每一帧都是国画壁纸。",
                "{\"studio\":\"六道无鱼\",\"year\":2023,\"genre\":\"战斗, 中国风, 热血\",\"origin\":\"国漫\",\"videos\":{\"bilibili\":\"//player.bilibili.com/player.html?bvid=BV14V4y1m7LV\"}}",
                "https://picsum.photos/seed/wushan/600/400", "//player.bilibili.com/player.html?bvid=BV14V4y1m7LV");
        i("anime", "拾荒者统治", "scavengers-reign",
                "外星坠毁幸存者的超现实求生之旅。诡异又美丽的生态系统，年度最佳科幻动画之一。",
                "{\"studio\":\"Titmouse×Max\",\"year\":2023,\"genre\":\"科幻, 生存, 奇幻\",\"origin\":\"美漫\",\"videos\":{\"bilibili\":\"//player.bilibili.com/player.html?bvid=BV1yC4y1K7GM\"}}",
                "https://picsum.photos/seed/scavengers/600/400", "//player.bilibili.com/player.html?bvid=BV1yC4y1K7GM");
        i("anime", "蓝色监狱", "blue-lock",
                "300 名前锋被关进蓝色监狱，淘汰到只剩一人。最狂足球动画，利己主义者的生存游戏。",
                "{\"studio\":\"8bit\",\"year\":2022,\"genre\":\"运动, 竞技, 热血\",\"origin\":\"日漫\",\"videos\":{\"bilibili\":\"//player.bilibili.com/player.html?bvid=BV1CG4y1f7nR\"}}",
                "https://picsum.photos/seed/bluelock/600/400", "//player.bilibili.com/player.html?bvid=BV1CG4y1f7nR");
        log.info("Anime seed: {} items", 10);
    }

    // Placeholder for inline insert in seedAnimeIfMissing
    private void i(String type, String title, String slug, String desc, String infoJson, String cover, String media) {
        Item item = new Item();
        item.setType(type); item.setTitle(title); item.setSlug(slug);
        item.setDescription(desc); item.setInfoJson(infoJson);
        item.setCoverUrl(cover); item.setMediaUrl(media);
        item.setSource("manual"); item.setStatus(1);
        itemMapper.insert(item);
    }

    private void insert(String type, String title, String slug, String description, String infoJson) {
        insert(type, title, slug, description, infoJson, null, null, null);
    }

    private void insert(String type, String title, String slug, String description, String infoJson,
                        String coverUrl, String externalLink) {
        insert(type, title, slug, description, infoJson, coverUrl, externalLink, null);
    }

    private void insert(String type, String title, String slug, String description, String infoJson,
                        String coverUrl, String externalLink, String mediaUrl) {
        Item item = new Item();
        item.setType(type);
        item.setTitle(title);
        item.setSlug(slug);
        item.setDescription(description);
        item.setInfoJson(infoJson);
        item.setCoverUrl(coverUrl);
        item.setExternalLink(externalLink);
        item.setMediaUrl(mediaUrl);
        item.setSource("manual");
        item.setStatus(1);
        itemMapper.insert(item);
    }

    private void seedUser() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        if (count != null && count > 0) return;

        jdbcTemplate.update(
                "INSERT INTO users (username, email, password_hash, role) VALUES (?, ?, ?, ?)",
                "admin", "admin@demonet.local", passwordEncoder.encode("changeme"), "ADMIN");
        log.info("Default admin user created: admin / changeme");
    }
}
