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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
        Map.entry("neon-genesis-evangelion-manga", List.of("manga", "mecha", "psychological")),
        Map.entry("sony-wh1000xm5", List.of("audio", "anc", "headphone", "sony")),
        Map.entry("airpods-pro-2", List.of("audio", "anc", "earbuds", "apple")),
        Map.entry("cherry-mx-board", List.of("keyboard", "mechanical", "cherry", "rgb")),
        Map.entry("ethiopia-yirgacheffe", List.of("coffee", "ethiopia", "washed", "light-roast", "fruity")),
        Map.entry("colombia-huila", List.of("coffee", "colombia", "washed", "medium-roast", "caramel")),
        Map.entry("guatemala-antigua", List.of("coffee", "guatemala", "washed", "dark-roast", "chocolate")),
        Map.entry("kenya-aa", List.of("coffee", "kenya", "washed", "light-roast", "floral")),
        Map.entry("indonesia-mandheling", List.of("coffee", "indonesia", "wet-hulled", "dark-roast", "herbal")),
        Map.entry("costa-rica-tarrazu", List.of("coffee", "costa-rica", "honey", "medium-roast", "sweet")),
        Map.entry("panama-geisha", List.of("coffee", "panama", "washed", "light-roast", "floral")),
        Map.entry("yunnan-arabica", List.of("coffee", "china", "natural", "dark-roast", "winey")),
        Map.entry("teamlab-borderless", List.of("offline", "exhibition", "immersive", "shanghai")),
        Map.entry("sleep-no-more", List.of("offline", "theater", "immersive", "shanghai")),
        Map.entry("misen-escape", List.of("offline", "escape-room", "horror", "beijing")),
        Map.entry("common-rare-market", List.of("offline", "market", "design", "shanghai")),
        Map.entry("omnipotent-youth-live", List.of("offline", "live", "rock", "tour")),
        Map.entry("pottery-workshop", List.of("offline", "workshop", "ceramics", "handmade")),
        Map.entry("wukang-road-citywalk", List.of("offline", "citywalk", "architecture", "walking")),
        Map.entry("ando-exhibition", List.of("offline", "exhibition", "architecture", "beijing"))
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
            seedCoffeeIfMissing();
            seedCQICoffeeIfMissing();
            seedOfflineIfMissing();
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
        insert("music", "七里香", "qilixiang",
                "周杰伦经典专辑。中国风与 R&B 的完美融合，《七里香》《借口》《将军》首首金曲。",
                "{\"artist\":\"周杰伦\",\"year\":2004,\"genre\":\"流行, R&B\",\"tracks\":10}");
        insert("music", "范特西", "fantasy-jay",
                "周杰伦封神之作。双截棍、爱在西元前、安静，华语乐坛里程碑。",
                "{\"artist\":\"周杰伦\",\"year\":2001,\"genre\":\"流行, Hip-Hop\",\"tracks\":10}");
        insert("music", "伟大的渺小", "great-small",
                "林俊杰沉淀之作。黑夜中寻找光明的温柔力量，编曲极致细腻。",
                "{\"artist\":\"林俊杰\",\"year\":2017,\"genre\":\"流行\",\"tracks\":11}");
        insert("music", "Faye Best", "faye-best",
                "王菲精选集。天空、红豆、光之翼，空灵嗓音的永恒记录。",
                "{\"artist\":\"王菲\",\"year\":2002,\"genre\":\"流行\",\"tracks\":16}");
        insert("music", "黑色柳丁", "black-tangerine",
                "陶喆 R&B 巅峰。黑色柳丁、Angel、Dear God，华语 R&B 圣经。",
                "{\"artist\":\"陶喆\",\"year\":2002,\"genre\":\"R&B, 摇滚\",\"tracks\":13}");
        insert("music", "第二人生", "second-life",
                "五月天概念专辑。如果世界末日，你要怎么度过你的第二人生？",
                "{\"artist\":\"五月天\",\"year\":2011,\"genre\":\"摇滚, 流行\",\"tracks\":12}");
        insert("music", "1989", "taylor-1989",
                "Taylor Swift 正式转型流行之作。Shake It Off、Blank Space、Style，流行乐教科书。",
                "{\"artist\":\"Taylor Swift\",\"year\":2014,\"genre\":\"流行, Synth-pop\",\"tracks\":13}");

        insert("digital", "Sony WH-1000XM5", "sony-wh1000xm5",
                "Sony 旗舰降噪耳机。自适应降噪、30 小时续航，通勤与专注的终极伴侣。",
                "{\"brand\":\"Sony\",\"category\":\"耳机\",\"year\":2022,\"features\":\"主动降噪,蓝牙5.2,30h续航\"}");
        insert("digital", "Apple AirPods Pro 2", "airpods-pro-2",
                "Apple H2 芯片加持，空间音频与自适应通透模式。降噪提升 2 倍。",
                "{\"brand\":\"Apple\",\"category\":\"耳机\",\"year\":2022,\"features\":\"空间音频,自适应降噪,MagSafe\"}");
        insert("digital", "Cherry MX Board 3.0S", "cherry-mx-board",
                "Cherry MX 红轴，无钢板设计回归纯粹机械手感。办公游戏兼备。",
                "{\"brand\":\"Cherry\",\"category\":\"键盘\",\"year\":2021,\"features\":\"MX红轴,RGB背光,109键\"}");

        insert("digital", "小米14 Ultra", "xiaomi-14-ultra",
                "小米影像旗舰。徕卡光学 Summilux 镜头，1 英寸 LYT-900 主摄，骁龙 8 Gen 3 旗舰平台。",
                "{\"brand\":\"小米\",\"category\":\"手机\",\"year\":2024,\"features\":\"徕卡光学,骁龙8Gen3,1英寸主摄,120W快充\"}");
        insert("digital", "iPad Pro M4", "ipad-pro-m4",
                "Apple M4 芯片首发，双层 OLED 超精视网膜 XDR 屏，5.1mm 极致轻薄。",
                "{\"brand\":\"Apple\",\"category\":\"平板\",\"year\":2024,\"features\":\"M4芯片,OLED屏,超薄5.1mm,Apple Pencil Pro\"}");
        insert("digital", "Galaxy Watch Ultra", "galaxy-watch-ultra",
                "三星腕上旗舰。钛金属表壳，3nm 处理器，100m 防水，AI 健康监测。",
                "{\"brand\":\"Samsung\",\"category\":\"手表\",\"year\":2024,\"features\":\"钛金属,Ti芯片,100m防水,AI健康\"}");
        insert("digital", "Kindle Scribe", "kindle-scribe",
                "首款带手写笔的 Kindle。10.2 英寸 300ppi 墨水屏，即读即写，数周续航。",
                "{\"brand\":\"Amazon\",\"category\":\"平板\",\"year\":2023,\"features\":\"10.2寸墨水屏,手写笔,300ppi,数周续航\"}");
        insert("digital", "ROG Ally X", "rog-ally-x",
                "华硕掌机升级版。Ryzen Z1 Extreme + 80Wh 超大电池，7 英寸 120Hz VRR 屏。",
                "{\"brand\":\"ASUS\",\"category\":\"掌机\",\"year\":2024,\"features\":\"Ryzen Z1E,80Wh电池,7寸120Hz,VRR\"}");
        insert("digital", "Logitech MX Master 3S", "mx-master-3s",
                "罗技旗舰办公鼠标。8K DPI 暗场追踪，MagSpeed 电磁滚轮一秒千行，静音按键。",
                "{\"brand\":\"Logitech\",\"category\":\"鼠标\",\"year\":2022,\"features\":\"8K DPI,电磁滚轮,静音按键,MagSpeed\"}");
        insert("digital", "华为MateBook X Pro", "matebook-x-pro",
                "华为旗舰轻薄本。酷睿 Ultra 处理器，OLED 原色屏，980g 轻至极致。",
                "{\"brand\":\"华为\",\"category\":\"笔记本\",\"year\":2024,\"features\":\"酷睿Ultra,OLED触屏,980g,40Gbps雷电4\"}");

        insert("coffee", "埃塞俄比亚 耶加雪菲", "ethiopia-yirgacheffe",
                "柑橘与茉莉的经典组合，柠檬酸明亮活泼。每一次手冲都是对埃塞高原的致敬。",
                "{\"origin\":\"埃塞俄比亚\",\"roast\":\"浅烘\",\"process\":\"水洗\",\"variety\":\"原生种\",\"flavor\":\"柑橘,茉莉,蜂蜜,柠檬酸\"}");
        insert("coffee", "哥伦比亚 蕙兰", "colombia-huila",
                "焦糖与坚果的温暖香气，柔和果酸。南美高原的平衡之作，每天早晨的第一杯。",
                "{\"origin\":\"哥伦比亚\",\"roast\":\"中烘\",\"process\":\"水洗\",\"variety\":\"铁皮卡\",\"flavor\":\"焦糖,坚果,可可,柔酸\"}");
        insert("coffee", "危地马拉 安提瓜", "guatemala-antigua",
                "火山土壤孕育的浓郁巧克力韵，烟熏尾调。三座火山环绕的传奇产区。",
                "{\"origin\":\"危地马拉\",\"roast\":\"中深烘\",\"process\":\"水洗\",\"variety\":\"波旁\",\"flavor\":\"巧克力,烟熏,香料,醇厚\"}");
        insert("coffee", "肯尼亚 AA", "kenya-aa",
                "乌梅与黑加仑的爆炸果酸，明亮如肯尼亚的阳光。SL28/SL34 的完美演绎。",
                "{\"origin\":\"肯尼亚\",\"roast\":\"浅烘\",\"process\":\"水洗\",\"variety\":\"SL28\",\"flavor\":\"乌梅,番茄,黑加仑,明亮酸\"}");
        insert("coffee", "印尼 曼特宁", "indonesia-mandheling",
                "草药与黑巧的厚重底蕴，焦糖回甘。苏门答腊湿刨法的独特魅力。",
                "{\"origin\":\"印尼\",\"roast\":\"深烘\",\"process\":\"湿刨法\",\"variety\":\"曼特宁\",\"flavor\":\"草药,黑巧,焦糖,厚重\"}");
        insert("coffee", "哥斯达黎加 塔拉珠", "costa-rica-tarrazu",
                "蜂蜜与太妃糖的甜蜜拥抱，顺滑如丝。蜜处理带来的惊人甜感。",
                "{\"origin\":\"哥斯达黎加\",\"roast\":\"中烘\",\"process\":\"蜜处理\",\"variety\":\"卡杜艾\",\"flavor\":\"蜂蜜,杏桃,太妃糖,顺滑\"}");
        insert("coffee", "巴拿马 瑰夏", "panama-geisha",
                "花香与佛手柑的优雅交响，蜜桃茶感。竞标级别的传奇咖啡，一生必尝。",
                "{\"origin\":\"巴拿马\",\"roast\":\"浅烘\",\"process\":\"水洗\",\"variety\":\"瑰夏\",\"flavor\":\"花香,佛手柑,蜜桃,茶感\"}");
        insert("coffee", "中国云南 小粒咖啡", "yunnan-arabica",
                "红酒与菠萝蜜的热带风情，黑糖甜醇。云南高海拔日晒的惊喜之作。",
                "{\"origin\":\"中国云南\",\"roast\":\"中深烘\",\"process\":\"日晒\",\"variety\":\"卡蒂姆\",\"flavor\":\"红酒,菠萝蜜,黑糖,热带果\"}");

        insert("offline", "teamLab 无界美术馆", "teamlab-borderless",
                "全球必看十大展览之一。6600㎡ 的光影宇宙，没有地图、没有路线，沉浸式探索无界艺术。",
                "{\"event_type\":\"展览\",\"venue\":\"上海·黄浦滨江\",\"date\":\"常设展（长期）\",\"time\":\"10:30-20:30（周一闭馆）\",\"price\":\"¥229/人\",\"capacity\":\"\",\"difficulty\":\"\",\"highlights\":\"互动装置,沉浸式,适合拍照,亲子友好\"}");
        insert("offline", "不眠之夜 Sleep No More", "sleep-no-more",
                "纽约爆红至今的浸入式戏剧。戴着白色面具，在 90 个房间里跟随演员跑上跑下 3 小时。",
                "{\"event_type\":\"沉浸式剧场\",\"venue\":\"上海·麦金侬酒店\",\"date\":\"常驻演出（长期）\",\"time\":\"19:00/19:20/19:40 三场\",\"price\":\"¥590/人起\",\"capacity\":\"\",\"difficulty\":\"\",\"highlights\":\"浸入式,莎剧改编,3小时,面具,酒店探险\"}");
        insert("offline", "弥生·日式沉浸密室", "misen-escape",
                "X先生金牌密室。日式惊悚氛围拉满，实景搭建超过 200㎡，剧情反转令人头皮发麻。",
                "{\"event_type\":\"密室逃脱\",\"venue\":\"北京·朝阳大悦城\",\"date\":\"常驻（需预约）\",\"time\":\"10:00-22:00\",\"price\":\"¥198/人\",\"capacity\":\"4-6人/场\",\"difficulty\":\"★★★★☆\",\"highlights\":\"日式惊悚,实景,剧情反转,多结局\"}");
        insert("offline", "凡几 Common Rare 市集", "common-rare-market",
                "国内最好的设计市集之一。70+ 独立品牌、插画、手作、咖啡美食，每次主题都不同。",
                "{\"event_type\":\"市集\",\"venue\":\"上海·现所/其他场地\",\"date\":\"不定期举办（关注官方）\",\"time\":\"11:00-19:00\",\"price\":\"¥49/人（早鸟）\",\"capacity\":\"\",\"difficulty\":\"\",\"highlights\":\"设计,手作,插画,美食,独立品牌\"}");
        insert("offline", "万能青年旅店 全国巡演", "omnipotent-youth-live",
                "万青十年磨一剑，《冀西南林路行》巡演。万人合唱《杀死那个石家庄人》的夜晚。",
                "{\"event_type\":\"Live\",\"venue\":\"多城市巡演\",\"date\":\"2024-2025 巡演季\",\"time\":\"19:30 开场\",\"price\":\"¥380/580/880\",\"capacity\":\"\",\"difficulty\":\"\",\"highlights\":\"摇滚,独立,全场大合唱,必看现场\"}");
        insert("offline", "陶艺拉坯体验课", "pottery-workshop",
                "零基础友好。2 小时从揉泥到拉坯成型，每人带走 2 件作品，老师全程指导。",
                "{\"event_type\":\"工作坊\",\"venue\":\"上海·永康路\",\"date\":\"每周六/日（需预约）\",\"time\":\"14:00-16:00\",\"price\":\"¥268/人\",\"capacity\":\"6人/班\",\"difficulty\":\"\",\"highlights\":\"手作,零基础,2小时,带走成品,解压\"}");
        insert("offline", "武康路历史建筑 Citywalk", "wukang-road-citywalk",
                "2 小时步行探索上海最优雅的街道。武康大楼、巴金故居、罗密欧阳台、密丹公寓。",
                "{\"event_type\":\"城市探索\",\"venue\":\"上海·武康路（集合：武康大楼）\",\"date\":\"每周六/日\",\"time\":\"09:30-11:30\",\"price\":\"¥128/人\",\"capacity\":\"15人/团\",\"difficulty\":\"\",\"highlights\":\"历史建筑,步行2h,专业讲解,适合拍照\"}");
        insert("offline", "安藤忠雄 回顾展", "ando-exhibition",
                "普利兹克奖得主安藤忠雄大型回顾展。光之教堂 1:1 再现，200+ 手稿与模型。",
                "{\"event_type\":\"展览\",\"venue\":\"北京·UCCA 尤伦斯当代艺术中心\",\"date\":\"2024.10.01 - 2025.01.19\",\"time\":\"10:00-18:30（周一闭馆）\",\"price\":\"¥150/人\",\"capacity\":\"\",\"difficulty\":\"\",\"highlights\":\"建筑模型,手稿,光之教堂,清水混凝土,大师\"}");

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

    private void seedCoffeeIfMissing() {
        Long count = itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getType, "coffee"));
        if (count != null && count > 0) return;
        log.info("Coffee data missing, seeding...");
        i("coffee", "埃塞俄比亚 耶加雪菲", "ethiopia-yirgacheffe",
                "柑橘与茉莉的经典组合，柠檬酸明亮活泼。每一次手冲都是对埃塞高原的致敬。",
                "{\"origin\":\"埃塞俄比亚\",\"roast\":\"浅烘\",\"process\":\"水洗\",\"variety\":\"原生种\",\"flavor\":\"柑橘,茉莉,蜂蜜,柠檬酸\",\"videos\":{\"bilibili\":\"\",\"youtube\":\"\"}}",
                "https://picsum.photos/seed/yirgacheffe/600/400", null);
        i("coffee", "哥伦比亚 蕙兰", "colombia-huila",
                "焦糖与坚果的温暖香气，柔和果酸。南美高原的平衡之作，每天早晨的第一杯。",
                "{\"origin\":\"哥伦比亚\",\"roast\":\"中烘\",\"process\":\"水洗\",\"variety\":\"铁皮卡\",\"flavor\":\"焦糖,坚果,可可,柔酸\",\"videos\":{\"bilibili\":\"\",\"youtube\":\"\"}}",
                "https://picsum.photos/seed/colombia/600/400", null);
        i("coffee", "危地马拉 安提瓜", "guatemala-antigua",
                "火山土壤孕育的浓郁巧克力韵，烟熏尾调。三座火山环绕的传奇产区。",
                "{\"origin\":\"危地马拉\",\"roast\":\"中深烘\",\"process\":\"水洗\",\"variety\":\"波旁\",\"flavor\":\"巧克力,烟熏,香料,醇厚\",\"videos\":{\"bilibili\":\"\",\"youtube\":\"\"}}",
                "https://picsum.photos/seed/guatemala/600/400", null);
        i("coffee", "肯尼亚 AA", "kenya-aa",
                "乌梅与黑加仑的爆炸果酸，明亮如肯尼亚的阳光。SL28/SL34 的完美演绎。",
                "{\"origin\":\"肯尼亚\",\"roast\":\"浅烘\",\"process\":\"水洗\",\"variety\":\"SL28\",\"flavor\":\"乌梅,番茄,黑加仑,明亮酸\",\"videos\":{\"bilibili\":\"\",\"youtube\":\"\"}}",
                "https://picsum.photos/seed/kenya/600/400", null);
        i("coffee", "印尼 曼特宁", "indonesia-mandheling",
                "草药与黑巧的厚重底蕴，焦糖回甘。苏门答腊湿刨法的独特魅力。",
                "{\"origin\":\"印尼\",\"roast\":\"深烘\",\"process\":\"湿刨法\",\"variety\":\"曼特宁\",\"flavor\":\"草药,黑巧,焦糖,厚重\",\"videos\":{\"bilibili\":\"\",\"youtube\":\"\"}}",
                "https://picsum.photos/seed/mandheling/600/400", null);
        i("coffee", "哥斯达黎加 塔拉珠", "costa-rica-tarrazu",
                "蜂蜜与太妃糖的甜蜜拥抱，顺滑如丝。蜜处理带来的惊人甜感。",
                "{\"origin\":\"哥斯达黎加\",\"roast\":\"中烘\",\"process\":\"蜜处理\",\"variety\":\"卡杜艾\",\"flavor\":\"蜂蜜,杏桃,太妃糖,顺滑\",\"videos\":{\"bilibili\":\"\",\"youtube\":\"\"}}",
                "https://picsum.photos/seed/costarica/600/400", null);
        i("coffee", "巴拿马 瑰夏", "panama-geisha",
                "花香与佛手柑的优雅交响，蜜桃茶感。竞标级别的传奇咖啡，一生必尝。",
                "{\"origin\":\"巴拿马\",\"roast\":\"浅烘\",\"process\":\"水洗\",\"variety\":\"瑰夏\",\"flavor\":\"花香,佛手柑,蜜桃,茶感\",\"videos\":{\"bilibili\":\"\",\"youtube\":\"\"}}",
                "https://picsum.photos/seed/geisha/600/400", null);
        i("coffee", "中国云南 小粒咖啡", "yunnan-arabica",
                "红酒与菠萝蜜的热带风情，黑糖甜醇。云南高海拔日晒的惊喜之作。",
                "{\"origin\":\"中国云南\",\"roast\":\"中深烘\",\"process\":\"日晒\",\"variety\":\"卡蒂姆\",\"flavor\":\"红酒,菠萝蜜,黑糖,热带果\",\"videos\":{\"bilibili\":\"\",\"youtube\":\"\"}}",
                "https://picsum.photos/seed/yunnan/600/400", null);
        log.info("Coffee seed: {} items", 8);
    }

    private void seedCQICoffeeIfMissing() {
        var wrapper = new LambdaQueryWrapper<Item>()
                .eq(Item::getType, "coffee")
                .eq(Item::getSource, "cqi");
        if (itemMapper.selectCount(wrapper) > 0) return;
        log.info("CQI coffee data missing, importing from CSV...");
        int imported = 0;
        try (var reader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("data/arabica_coffee.csv"),
                StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null && imported < 100) {
                String[] cols = parseCsvLine(line);
                if (cols.length < 44) continue;
                double totalPoints = parseDouble(cols[30]); // Total.Cup.Points
                if (totalPoints < 86.0) continue;
                String country = cols[3];   // Country.of.Origin
                String region = cols[10];   // Region
                String farm = cols[4];      // Farm.Name
                String variety = cols[18];  // Variety
                String process = cols[19];  // Processing.Method
                String altitude = cols[43]; // altitude_mean_meters
                String harvest = cols[15];  // Harvest.Year
                String producer = cols[11]; // Producer

                String title = buildCQITitle(country, region, farm, variety);
                String slug = buildCQISlug(country, region, farm, variety, imported);
                String desc = buildCQIDescription(country, region, process, totalPoints);
                String infoJson = buildCQIInfoJson(country, region, farm, producer, variety,
                        process, altitude, harvest, cols);
                i("coffee", title, slug, desc, infoJson,
                        "https://picsum.photos/seed/coffee-" + imported + "/600/400", null);
                // Mark source as CQI
                var latest = itemMapper.selectList(new LambdaQueryWrapper<Item>()
                        .eq(Item::getSlug, slug));
                if (!latest.isEmpty()) {
                    latest.get(0).setSource("cqi");
                    itemMapper.updateById(latest.get(0));
                }
                imported++;
            }
        } catch (Exception e) {
            log.error("Failed to import CQI coffee data: {}", e.getMessage());
        }
        log.info("CQI coffee imported: {} items", imported);
    }

    private String buildCQITitle(String country, String region, String farm, String variety) {
        StringBuilder sb = new StringBuilder();
        if (country != null && !country.isBlank()) sb.append(country);
        if (region != null && !region.isBlank()) sb.append(" ").append(region);
        if (farm != null && !farm.isBlank() && !farm.equalsIgnoreCase(country))
            sb.append(" · ").append(farm);
        if (variety != null && !variety.isBlank() && !variety.equals("Other"))
            sb.append(" (").append(variety).append(")");
        return sb.toString().trim();
    }

    private String buildCQISlug(String country, String region, String farm, String variety, int idx) {
        String base = "cqi-";
        if (country != null && !country.isBlank()) base += country.toLowerCase().replaceAll("[^a-z0-9]+", "-") + "-";
        if (region != null && !region.isBlank()) base += region.toLowerCase().replaceAll("[^a-z0-9]+", "-") + "-";
        base += idx;
        return base.replaceAll("-+$", "").replaceAll("-{2,}", "-");
    }

    private String buildCQIDescription(String country, String region, String process, double score) {
        StringBuilder sb = new StringBuilder();
        sb.append("CQI SCA 评分 ").append(String.format("%.2f", score)).append(" 分");
        if (country != null && !country.isBlank()) sb.append(" · 产地 ").append(country);
        if (region != null && !region.isBlank()) sb.append(" ").append(region);
        if (process != null && !process.isBlank()) sb.append(" · ").append(process);
        sb.append("。经 Q-Grader 专业品测认证。");
        return sb.toString();
    }

    private String buildCQIInfoJson(String country, String region, String farm, String producer,
                                     String variety, String process, String altitude, String harvest,
                                     String[] cols) {
        StringBuilder json = new StringBuilder("{");
        appendJsonField(json, "origin", country);
        appendJsonField(json, "region", region);
        appendJsonField(json, "farm", farm);
        appendJsonField(json, "producer", producer);
        appendJsonField(json, "variety", variety);
        appendJsonField(json, "process", process);
        appendJsonField(json, "altitude", altitude);
        appendJsonField(json, "harvest", harvest);
        // SCA scores
        appendJsonNumber(json, "aroma", parseDouble(cols[20]));
        appendJsonNumber(json, "flavor", parseDouble(cols[21]));
        appendJsonNumber(json, "aftertaste", parseDouble(cols[22]));
        appendJsonNumber(json, "acidity", parseDouble(cols[23]));
        appendJsonNumber(json, "body", parseDouble(cols[24]));
        appendJsonNumber(json, "balance", parseDouble(cols[25]));
        appendJsonNumber(json, "uniformity", parseDouble(cols[26]));
        appendJsonNumber(json, "clean_cup", parseDouble(cols[27]));
        appendJsonNumber(json, "sweetness", parseDouble(cols[28]));
        appendJsonNumber(json, "cupper_points", parseDouble(cols[29]));
        appendJsonNumber(json, "total_cup_points", parseDouble(cols[30]));
        appendJsonField(json, "flavor_notes", buildFlavorString(cols));
        // videos last — strip trailing comma
        json.append("\"videos\":{\"youtube\":\"\",\"bilibili\":\"\"}}");
        // Remove trailing comma before videos if present
        String result = json.toString();
        if (result.endsWith(",\"videos\":")) {
            result = result.substring(0, result.length() - 10) + "\"videos\":{\"youtube\":\"\",\"bilibili\":\"\"}}";
        }
        return result;
    }

    private String buildFlavorString(String[] cols) {
        // Try to extract meaningful flavor descriptors from the scores
        // Higher Aroma+Flavor → fruity/floral, higher Body → chocolate/nutty
        double aroma = parseDouble(cols[20]);
        double flavor = parseDouble(cols[21]);
        double body = parseDouble(cols[24]);
        StringBuilder flavors = new StringBuilder();
        if (aroma > 8.0 && flavor > 8.0) flavors.append("花果香,明亮");
        else if (body > 8.0) flavors.append("巧克力,坚果,醇厚");
        else flavors.append("均衡,柔和");
        if (body > 8.5) flavors.append(",厚重");
        return flavors.toString();
    }

    private void appendJsonField(StringBuilder sb, String key, String value) {
        if (value != null && !value.isBlank()) {
            sb.append("\"").append(key).append("\":\"").append(escapeJson(value)).append("\",");
        }
    }

    private void appendJsonNumber(StringBuilder sb, String key, double value) {
        if (value > 0) {
            sb.append("\"").append(key).append("\":").append(value).append(",");
        }
    }

    private void appendJsonRaw(StringBuilder sb, String key, String value) {
        sb.append("\"").append(key).append("\":").append(value).append(",");
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0; }
    }

    private String[] parseCsvLine(String line) {
        // Simple CSV parser handling quoted fields
        java.util.List<String> fields = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    private void seedOfflineIfMissing() {
        Long count = itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getType, "offline"));
        if (count != null && count > 0) return;
        log.info("Offline data missing, seeding...");
        i("offline", "teamLab 无界美术馆", "teamlab-borderless",
                "全球必看十大展览之一。6600㎡ 的光影宇宙，没有地图、没有路线，沉浸式探索无界艺术。",
                "{\"event_type\":\"展览\",\"venue\":\"上海·黄浦滨江\",\"date\":\"常设展（长期）\",\"time\":\"10:30-20:30（周一闭馆）\",\"price\":\"¥229/人\",\"highlights\":\"互动装置,沉浸式,适合拍照,亲子友好\",\"videos\":{\"bilibili\":\"\",\"youtube\":\"\"}}",
                "https://picsum.photos/seed/teamlab/600/400", null);
        i("offline", "不眠之夜 Sleep No More", "sleep-no-more",
                "纽约爆红至今的浸入式戏剧。戴着白色面具，在 90 个房间里跟随演员跑上跑下 3 小时。",
                "{\"event_type\":\"沉浸式剧场\",\"venue\":\"上海·麦金侬酒店\",\"date\":\"常驻演出（长期）\",\"time\":\"19:00/19:20/19:40 三场\",\"price\":\"¥590/人起\",\"highlights\":\"浸入式,莎剧改编,3小时,面具,酒店探险\",\"videos\":{\"bilibili\":\"\",\"youtube\":\"\"}}",
                "https://picsum.photos/seed/sleepnomore/600/400", null);
        i("offline", "弥生·日式沉浸密室", "misen-escape",
                "X先生金牌密室。日式惊悚氛围拉满，实景搭建超过 200㎡，剧情反转令人头皮发麻。",
                "{\"event_type\":\"密室逃脱\",\"venue\":\"北京·朝阳大悦城\",\"date\":\"常驻（需预约）\",\"time\":\"10:00-22:00\",\"price\":\"¥198/人\",\"capacity\":\"4-6人/场\",\"difficulty\":\"★★★★☆\",\"highlights\":\"日式惊悚,实景,剧情反转,多结局\",\"videos\":{\"bilibili\":\"\",\"youtube\":\"\"}}",
                "https://picsum.photos/seed/misen/600/400", null);
        i("offline", "凡几 Common Rare 市集", "common-rare-market",
                "国内最好的设计市集之一。70+ 独立品牌、插画、手作、咖啡美食，每次主题都不同。",
                "{\"event_type\":\"市集\",\"venue\":\"上海·现所/其他场地\",\"date\":\"不定期举办（关注官方）\",\"time\":\"11:00-19:00\",\"price\":\"¥49/人（早鸟）\",\"highlights\":\"设计,手作,插画,美食,独立品牌\",\"videos\":{\"bilibili\":\"\",\"youtube\":\"\"}}",
                "https://picsum.photos/seed/market/600/400", null);
        i("offline", "万能青年旅店 全国巡演", "omnipotent-youth-live",
                "万青十年磨一剑，《冀西南林路行》巡演。万人合唱《杀死那个石家庄人》的夜晚。",
                "{\"event_type\":\"Live\",\"venue\":\"多城市巡演\",\"date\":\"2024-2025 巡演季\",\"time\":\"19:30 开场\",\"price\":\"¥380/580/880\",\"highlights\":\"摇滚,独立,全场大合唱,必看现场\",\"videos\":{\"bilibili\":\"\",\"youtube\":\"\"}}",
                "https://picsum.photos/seed/wanqing/600/400", null);
        i("offline", "陶艺拉坯体验课", "pottery-workshop",
                "零基础友好。2 小时从揉泥到拉坯成型，每人带走 2 件作品，老师全程指导。",
                "{\"event_type\":\"工作坊\",\"venue\":\"上海·永康路\",\"date\":\"每周六/日（需预约）\",\"time\":\"14:00-16:00\",\"price\":\"¥268/人\",\"capacity\":\"6人/班\",\"highlights\":\"手作,零基础,2小时,带走成品,解压\",\"videos\":{\"bilibili\":\"\",\"youtube\":\"\"}}",
                "https://picsum.photos/seed/pottery/600/400", null);
        i("offline", "武康路历史建筑 Citywalk", "wukang-road-citywalk",
                "2 小时步行探索上海最优雅的街道。武康大楼、巴金故居、罗密欧阳台、密丹公寓。",
                "{\"event_type\":\"城市探索\",\"venue\":\"上海·武康路（集合：武康大楼）\",\"date\":\"每周六/日\",\"time\":\"09:30-11:30\",\"price\":\"¥128/人\",\"capacity\":\"15人/团\",\"highlights\":\"历史建筑,步行2h,专业讲解,适合拍照\",\"videos\":{\"bilibili\":\"\",\"youtube\":\"\"}}",
                "https://picsum.photos/seed/citywalk/600/400", null);
        i("offline", "安藤忠雄 回顾展", "ando-exhibition",
                "普利兹克奖得主安藤忠雄大型回顾展。光之教堂 1:1 再现，200+ 手稿与模型。",
                "{\"event_type\":\"展览\",\"venue\":\"北京·UCCA 尤伦斯当代艺术中心\",\"date\":\"2024.10.01 - 2025.01.19\",\"time\":\"10:00-18:30（周一闭馆）\",\"price\":\"¥150/人\",\"highlights\":\"建筑模型,手稿,光之教堂,清水混凝土,大师\",\"videos\":{\"bilibili\":\"\",\"youtube\":\"\"}}",
                "https://picsum.photos/seed/ando/600/400", null);
        log.info("Offline seed: {} items", 8);
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
