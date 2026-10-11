package com.dullbits.drawbot.color;

import java.awt.Color;
import java.util.*;

/**
 * Copic marker catalog, color distance algorithms, and standard palette presets.
 * Contains all 358 original Copic markers and the 18 standard Drawbot marker sets.
 */
public final class CopicPalette {

    public static final class Marker {
        public final String code;
        public final int rgb;
        public final String name;

        public Marker(String code, int rgb, String name) {
            this.code = code;
            this.rgb = rgb;
            this.name = name;
        }

        public Color toColor() {
            return new Color(rgb);
        }
    }

    public static final class MarkerSet {
        public final String name;
        public final String[] markerCodes;

        public MarkerSet(String name, String... markerCodes) {
            this.name = name;
            this.markerCodes = markerCodes;
        }
    }

    private static final Map<String, Marker> ORIGINAL_MARKERS = new LinkedHashMap<>(400);
    private static final Map<String, Marker> SKETCH_MARKERS = new LinkedHashMap<>(400);

    public static final List<MarkerSet> PRESETS = new ArrayList<>();

    static {
        putOriginal("0", 0xffffff, "Colorless Blender");
        putOriginal("100", 0x312b2b, "Black");
        putOriginal("110", 0x030708, "Special Black");
        putOriginal("B00", 0xddf0f4, "Frost Blue");
        putOriginal("B01", 0xd6eef2, "Mint Blue");
        putOriginal("B02", 0xb3e3f1, "Robin's Egg Blue");
        putOriginal("B04", 0x73cfe6, "Tahitian Blue");
        putOriginal("B05", 0x40c5e6, "Process Blue");
        putOriginal("B06", 0x00b3e6, "Peacok Blue");
        putOriginal("B12", 0xc8e6f0, "Ice Blue");
        putOriginal("B14", 0x71cfeb, "Light Blue");
        putOriginal("B16", 0x00bcea, "Cyanine Blue");
        putOriginal("B18", 0x1d8acb, "Lapis Lazuli");
        putOriginal("B21", 0xdbedf9, "Baby Blue");
        putOriginal("B23", 0x92c2e8, "Phthalo Blue");
        putOriginal("B24", 0x8acef3, "Sky");
        putOriginal("B26", 0x65b3e3, "Cobalt Blue");
        putOriginal("B29", 0x0177c1, "Ultramarine");
        putOriginal("B32", 0xe2eff7, "Pale Blue");
        putOriginal("B34", 0x82c3ed, "Manganese Blue");
        putOriginal("B37", 0x156fa4, "Antwerp Blue");
        putOriginal("B39", 0x2b64a9, "Prussian Blue");
        putOriginal("B41", 0xe2f0fb, "Powder Blue");
        putOriginal("B45", 0x75c0ea, "Smoky Blue");
        putOriginal("BG02", 0xc6e8ea, "New Blue");
        putOriginal("BG05", 0x83d2e1, "Holiday Blue");
        putOriginal("BG09", 0x01b1c9, "Blue Green");
        putOriginal("BG10", 0xdcf0ef, "Cool Shadow");
        putOriginal("BG11", 0xceebf1, "Moon White");
        putOriginal("BG13", 0xc4e7e9, "Mint Green");
        putOriginal("BG15", 0xa0d9d2, "Aqua");
        putOriginal("BG18", 0x37c0b0, "Teal Blue");
        putOriginal("BG32", 0xbce2d7, "Aqua Mint");
        putOriginal("BG34", 0xa3dad7, "Horizon Green");
        putOriginal("BG45", 0xafdfdf, "Nile Blue");
        putOriginal("BG49", 0x00b6b9, "Duck Blue");
        putOriginal("BG99", 0x6e9b87, "Flagstone Blue");
        putOriginal("BV00", 0xe0dced, "Mauve Shadow");
        putOriginal("BV04", 0x7c97ce, "Blue Berry");
        putOriginal("BV08", 0x9d7eb9, "Blue Violet");
        putOriginal("BV23", 0xb1c0dd, "Grayish Lavender");
        putOriginal("BV31", 0xeae7f2, "Pale Lavender");
        putOriginal("C0", 0xe0e7ed, "Cool Gray");
        putOriginal("C10", 0x202b31, "Cool Gray");
        putOriginal("C1", 0xdae3e8, "Cool Gray No. 1");
        putOriginal("C2", 0xccd7dd, "Cool Gray");
        putOriginal("C3", 0xc1ccd2, "Cool Gray No. 3");
        putOriginal("C4", 0xa6b4bd, "Cool Gray");
        putOriginal("C5", 0x92a0ab, "Cool Gray No. 5");
        putOriginal("C6", 0x7b8c96, "Cool Gray");
        putOriginal("C7", 0x637079, "Cool Gray No. 7");
        putOriginal("C8", 0x535d66, "Cool Gray");
        putOriginal("C9", 0x3c474d, "Cool Gray");
        putOriginal("E00", 0xfdf3ea, "Cotton Pearl");
        putOriginal("E02", 0xfeece0, "Fruit Pink");
        putOriginal("E04", 0xe4bcc4, "Lipstick Natural");
        putOriginal("E07", 0xcc816a, "Light Mahogany");
        putOriginal("E09", 0xd96a4f, "Burnt Sienna");
        putOriginal("E11", 0xfee9d6, "Barley Beige");
        putOriginal("E13", 0xe9c5af, "Light Suntan");
        putOriginal("E15", 0xfbbb8d, "Dark Suntan");
        putOriginal("E19", 0xc45238, "Redwood");
        putOriginal("E21", 0xfde2c7, "Soft Sun");
        putOriginal("E25", 0xd2a482, "Caribe Cocoa");
        putOriginal("E27", 0x997663, "Milk Chocolate");
        putOriginal("E29", 0x884636, "Burnt Umber");
        putOriginal("E31", 0xf2e6ce, "Brick Beige");
        putOriginal("E33", 0xf3d2b1, "Sand");
        putOriginal("E34", 0xf0caa6, "Toast");
        putOriginal("E35", 0xe6c3a3, "Chamois");
        putOriginal("E37", 0xcc9159, "Sepia");
        putOriginal("E39", 0xc5743f, "Leather");
        putOriginal("E40", 0xf2e8dc, "Brick White");
        putOriginal("E41", 0xfef1e1, "Pearl White");
        putOriginal("E43", 0xe8dabd, "Dull Ivory");
        putOriginal("E44", 0xc5b9a9, "Clay");
        putOriginal("E49", 0x634c3c, "Dark Bark");
        putOriginal("E51", 0xfeecd6, "Milky White");
        putOriginal("E53", 0xf3e6c3, "Raw Silk");
        putOriginal("E55", 0xf1dfb9, "Light Camel");
        putOriginal("E57", 0xb18558, "Light Walnut");
        putOriginal("E59", 0x9a7f6c, "Walnut");
        putOriginal("E77", 0x7f604e, "Maroon");
        putOriginal("G00", 0xe3f2ed, "Jade Green");
        putOriginal("G02", 0xcfe8d3, "Spectrum Green");
        putOriginal("G05", 0x69c07b, "Emerald Green");
        putOriginal("G07", 0x7bc576, "Nile Green");
        putOriginal("G09", 0x7ac465, "Veronese Green");
        putOriginal("G12", 0xd2e8c4, "Sea Green");
        putOriginal("G14", 0x97cf90, "Apple Green");
        putOriginal("G16", 0x60c198, "Malachite");
        putOriginal("G17", 0x14b37d, "Forest Green");
        putOriginal("G19", 0x2db98a, "Bright Parrot Green");
        putOriginal("G20", 0xedf6db, "Wax White");
        putOriginal("G21", 0xc4e4cd, "Lime Green");
        putOriginal("G24", 0xc3e0b4, "Willow");
        putOriginal("G28", 0x119462, "Ocean Green");
        putOriginal("G29", 0x197c5d, "Pine Tree Green");
        putOriginal("G40", 0xe4f1df, "Dim Green");
        putOriginal("G82", 0xccdab9, "Spring Dim Green");
        putOriginal("G85", 0x9dc3aa, "Verdigris");
        putOriginal("G99", 0x5f7e3a, "Olive");
        putOriginal("N0", 0xeceeed, "Neutral Gray");
        putOriginal("N10", 0x312f30, "Neutral Gray");
        putOriginal("N1", 0xe2e3e5, "Neutral Gray");
        putOriginal("N2", 0xdadbdd, "Neutral Gray");
        putOriginal("N3", 0xd1d2d4, "Neutral Gray");
        putOriginal("N4", 0xbcbdc1, "Neutral Gray");
        putOriginal("N5", 0xa8a9ad, "Neutral Gray");
        putOriginal("N6", 0x949599, "Neutral Gray");
        putOriginal("N7", 0x77787c, "Neutral Gray");
        putOriginal("N8", 0x636466, "Neutral Gray");
        putOriginal("N9", 0x4c4d4f, "Neutral Gray");
        putOriginal("R00", 0xfeeae1, "Pinkish White");
        putOriginal("R02", 0xfdd3c7, "Rose Salmon");
        putOriginal("R05", 0xf6927b, "Salmon Red");
        putOriginal("R08", 0xf26754, "Vermilion");
        putOriginal("R11", 0xfde1d5, "Pale Cherry Pink");
        putOriginal("R17", 0xf4846c, "Lipstick Orange");
        putOriginal("R20", 0xfcd7cf, "Blush");
        putOriginal("R24", 0xf27579, "Prawn");
        putOriginal("R27", 0xf15062, "Cadmium Red");
        putOriginal("R29", 0xed174b, "Lipstick Red");
        putOriginal("R32", 0xfac1ba, "Peach");
        putOriginal("R35", 0xf27185, "Coral");
        putOriginal("R37", 0xe86c74, "Carmine");
        putOriginal("R39", 0xcb487a, "Garnet");
        putOriginal("R59", 0xb74f70, "Cardinal");
        putOriginal("RV02", 0xfad5e6, "Sugared Almond Pink");
        putOriginal("RV04", 0xf6a3bf, "Shock  Pink");
        putOriginal("RV06", 0xf386af, "Cerise");
        putOriginal("RV09", 0xe171ac, "Fuchsia");
        putOriginal("RV10", 0xfdecf4, "Pale Pink");
        putOriginal("RV11", 0xfbd6dd, "Pink");
        putOriginal("RV13", 0xf9c9d7, "Tender Pink");
        putOriginal("RV14", 0xf495b7, "Begonia Pink");
        putOriginal("RV17", 0xdb7eb3, "Deep Magenta");
        putOriginal("RV19", 0xd268aa, "Red Violet");
        putOriginal("RV21", 0xfde8e7, "Light Pink");
        putOriginal("RV25", 0xf493be, "Dog Rose Flower");
        putOriginal("RV29", 0xef4880, "Crimson");
        putOriginal("RV32", 0xfad3ce, "Shadow Pink");
        putOriginal("RV34", 0xf9afae, "Dark Pink");
        putOriginal("T0", 0xeceeed, "Toner Gray");
        putOriginal("T10", 0x322e2d, "Toner Gray");
        putOriginal("T1", 0xeaeae8, "Toner Gray");
        putOriginal("T2", 0xe0e0de, "Toner Gray");
        putOriginal("T3", 0xd1d2cc, "Toner Gray");
        putOriginal("T4", 0xbcbbb9, "Toner Gray");
        putOriginal("T5", 0xa8a7a3, "Toner Gray");
        putOriginal("T6", 0x949590, "Toner Gray");
        putOriginal("T7", 0x777674, "Toner Gray");
        putOriginal("T8", 0x63645f, "Toner Gray");
        putOriginal("T9", 0x4c4b49, "Toner Gray");
        putOriginal("V04", 0xe6aace, "Lilac");
        putOriginal("V06", 0xce95c2, "Lavender");
        putOriginal("V09", 0x8754a1, "Violet");
        putOriginal("V12", 0xeed7e9, "Pale Lilac");
        putOriginal("V15", 0xd3a6cd, "Mallow");
        putOriginal("V17", 0xa092c7, "Amethyst");
        putOriginal("W0", 0xecece4, "Warm Gray");
        putOriginal("W10", 0x302f2b, "Warm Gray");
        putOriginal("W1", 0xe7e7df, "Warm Gray No. 1");
        putOriginal("W2", 0xddddd5, "Warm Gray");
        putOriginal("W3", 0xd2d2ca, "Warm Gray No. 3");
        putOriginal("W4", 0xbcbdb7, "Warm Gray");
        putOriginal("W5", 0xa8a9a4, "Warm Gray No. 5");
        putOriginal("W6", 0x94958f, "Warm Gray");
        putOriginal("W7", 0x777873, "Warm Gray No. 7");
        putOriginal("W8", 0x63645f, "Warm Gray");
        putOriginal("W9", 0x4c4d48, "Warm Gray");
        putOriginal("Y00", 0xfefddf, "Barium Yellow");
        putOriginal("Y02", 0xf6f396, "Canary Yellow");
        putOriginal("Y06", 0xfef56c, "Yellow");
        putOriginal("Y08", 0xfef200, "Acid Yellow");
        putOriginal("Y11", 0xfffbcc, "Pale Yellow");
        putOriginal("Y13", 0xfbf7ae, "Lemon Yellow");
        putOriginal("Y15", 0xfee96c, "Cadmium Yellow");
        putOriginal("Y17", 0xffe455, "Golden Yellow");
        putOriginal("Y19", 0xffe93e, "Napoli Yellow");
        putOriginal("Y21", 0xffeec2, "Buttercup Yellow");
        putOriginal("Y23", 0xfbe3b3, "Yellowish Beige");
        putOriginal("Y26", 0xf0dd67, "Mustard");
        putOriginal("Y38", 0xffd374, "Honey");
        putOriginal("YG01", 0xe2ebb2, "Green Bice");
        putOriginal("YG03", 0xdeeaaa, "Yellow Green");
        putOriginal("YG05", 0xd6e592, "Salad");
        putOriginal("YG07", 0xa5cf4f, "Acid Green");
        putOriginal("YG09", 0x82c566, "Lettuce Green");
        putOriginal("YG11", 0xe5f0d0, "Mignonette");
        putOriginal("YG13", 0xd4e59f, "Chartreuse");
        putOriginal("YG17", 0x72c156, "Grass Green");
        putOriginal("YG21", 0xf7f6be, "Anise");
        putOriginal("YG23", 0xe6eb8f, "New Leaf");
        putOriginal("YG25", 0xd0e17b, "Celadon Green");
        putOriginal("YG41", 0xd5ebd4, "Pale Cobalt Green");
        putOriginal("YG45", 0xb4dcb7, "Cobalt Green");
        putOriginal("YG63", 0xa0caa2, "Pea Green");
        putOriginal("YG67", 0x81bf8c, "Moss");
        putOriginal("YG91", 0xdad7ae, "Putty");
        putOriginal("YG95", 0xcbc65e, "Pale Olive");
        putOriginal("YG97", 0x958f03, "Spanish Olive");
        putOriginal("YG99", 0x4e6a15, "Marine Green");
        putOriginal("YR00", 0xfed6bd, "Powder Pink");
        putOriginal("YR02", 0xfcdcc5, "Light Orange");
        putOriginal("YR04", 0xfec369, "Chrome Orange");
        putOriginal("YR07", 0xf26f39, "Cadmium Orange");
        putOriginal("YR09", 0xf15524, "Chinese Orange");
        putOriginal("YR14", 0xfec84e, "Caramel");
        putOriginal("YR16", 0xfeb729, "Apricot");
        putOriginal("YR18", 0xf26b3c, "Sanguine");
        putOriginal("YR21", 0xf5ddb1, "Cream");
        putOriginal("YR23", 0xeccf8b, "Yellow Ochre");
        putOriginal("YR24", 0xf0cf64, "Pale Sepia");
        putSketch("0", 0xffffff, "Colorless Blender");
        putSketch("100", 0x312b2b, "Black");
        putSketch("110", 0x030708, "Special Black");
        putSketch("B0000", 0xf0f9fe, "Pale Celestine");
        putSketch("B000", 0xe6f4f5, "Pale Porcelain Blue");
        putSketch("B00", 0xddf0f4, "Frost Blue");
        putSketch("B01", 0xd6eef2, "Mint Blue");
        putSketch("B02", 0xb3e3f1, "Robin's Egg Blue");
        putSketch("B04", 0x73cfe6, "Tahitian Blue");
        putSketch("B05", 0x40c5e6, "Process Blue");
        putSketch("B06", 0x00b3e6, "Peacok Blue");
        putSketch("B12", 0xc8e6f0, "Ice Blue");
        putSketch("B14", 0x71cfeb, "Light Blue");
        putSketch("B16", 0x00bcea, "Cyanine Blue");
        putSketch("B18", 0x1d8acb, "Lapis Lazuli");
        putSketch("B21", 0xdbedf9, "Baby Blue");
        putSketch("B23", 0x92c2e8, "Phthalo Blue");
        putSketch("B24", 0x8acef3, "Sky");
        putSketch("B26", 0x65b3e3, "Cobalt Blue");
        putSketch("B28", 0x196db6, "Royal Blue");
        putSketch("B29", 0x0177c1, "Ultramarine");
        putSketch("B32", 0xe2eff7, "Pale Blue");
        putSketch("B34", 0x82c3ed, "Manganese Blue");
        putSketch("B37", 0x156fa4, "Antwerp Blue");
        putSketch("B39", 0x2b64a9, "Prussian Blue");
        putSketch("B41", 0xe2f0fb, "Powder Blue");
        putSketch("B45", 0x75c0ea, "Smoky Blue");
        putSketch("B52", 0xadcddc, "Soft Greenish Blue");
        putSketch("B60", 0xdae1f3, "Pale Blue Gray");
        putSketch("B63", 0xa7bbe0, "Light Hydrangea");
        putSketch("B66", 0x6888c5, "Clematis");
        putSketch("B69", 0x2165ae, "Stratospheric Blue");
        putSketch("B79", 0x3b479d, "Iris");
        putSketch("B91", 0xd5e2eb, "Pale Grayish Blue");
        putSketch("B93", 0x95c1da, "Light Crockery Blue");
        putSketch("B95", 0x74a7c6, "Light Grayish Cobalt");
        putSketch("B97", 0x457a9a, "Night Blue");
        putSketch("B99", 0x0f547e, "Agate");
        putSketch("BG0000", 0xeff8f3, "Snow Green");
        putSketch("BG000", 0xe5f4ed, "Pale Aqua");
        putSketch("BG01", 0xc7e6fa, "Aqua Blue");
        putSketch("BG02", 0xc6e8ea, "New Blue");
        putSketch("BG05", 0x83d2e1, "Holiday Blue");
        putSketch("BG07", 0x1db8ce, "Petroleum Blue");
        putSketch("BG09", 0x01b1c9, "Blue Green");
        putSketch("BG10", 0xdcf0ef, "Cool Shadow");
        putSketch("BG11", 0xceebf1, "Moon White");
        putSketch("BG13", 0xc4e7e9, "Mint Green");
        putSketch("BG15", 0xa0d9d2, "Aqua");
        putSketch("BG18", 0x37c0b0, "Teal Blue");
        putSketch("BG23", 0xbde5dd, "Coral Sea");
        putSketch("BG32", 0xbce2d7, "Aqua Mint");
        putSketch("BG34", 0xa3dad7, "Horizon Green");
        putSketch("BG45", 0xafdfdf, "Nile Blue");
        putSketch("BG49", 0x00b6b9, "Duck Blue");
        putSketch("BG53", 0xaccfd1, "Ice Mint");
        putSketch("BG57", 0x64bebe, "Sketch Jasper");
        putSketch("BG70", 0xdaecee, "Ocean Mist");
        putSketch("BG72", 0x74b8bb, "Ice Ocean");
        putSketch("BG75", 0x59918e, "Abyss Green");
        putSketch("BG78", 0x49706b, "Bronze");
        putSketch("BG90", 0xe8ede7, "Sketch Gray Sky");
        putSketch("BG93", 0xbac1b9, "Green Gray");
        putSketch("BG96", 0x81a291, "Bush");
        putSketch("BG99", 0x6e9b87, "Flagstone Blue");
        putSketch("BV0000", 0xeae7f2, "Sketch Pale Thistle");
        putSketch("BV000", 0xeae7f2, "Iridescent Mauve");
        putSketch("BV00", 0xe0dced, "Mauve Shadow");
        putSketch("BV01", 0xc4c9e6, "Viola");
        putSketch("BV02", 0xaab8db, "Prune");
        putSketch("BV04", 0x7c97ce, "Blue Berry");
        putSketch("BV08", 0x9d7eb9, "Blue Violet");
        putSketch("BV11", 0xd4d2e8, "Soft Violet");
        putSketch("BV13", 0x8491c8, "Hydrangea Blue");
        putSketch("BV17", 0x6e84bd, "Deep Reddish Blue");
        putSketch("BV20", 0xcfdbf1, "Dull Lavender");
        putSketch("BV23", 0xb1c0dd, "Grayish Lavender");
        putSketch("BV25", 0x8184a7, "Grayish Violet");
        putSketch("BV29", 0x384558, "Slate");
        putSketch("BV31", 0xeae7f2, "Pale Lavender");
        putSketch("BV34", 0x9fa7bc, "Sketch Bluebell");
        putSketch("C00", 0xe8f0f3, "Cool Gray");
        putSketch("C0", 0xe0e7ed, "Cool Gray");
        putSketch("C10", 0x202b31, "Cool Gray");
        putSketch("C1", 0xdae3e8, "Cool Gray No. 1");
        putSketch("C2", 0xccd7dd, "Cool Gray");
        putSketch("C3", 0xc1ccd2, "Cool Gray No. 3");
        putSketch("C4", 0xa6b4bd, "Cool Gray");
        putSketch("C5", 0x92a0ab, "Cool Gray No. 5");
        putSketch("C6", 0x7b8c96, "Cool Gray");
        putSketch("C7", 0x637079, "Cool Gray No. 7");
        putSketch("C8", 0x535d66, "Cool Gray");
        putSketch("C9", 0x3c474d, "Cool Gray");
        putSketch("E0000", 0xfffaf4, "Floral White");
        putSketch("E000", 0xfef5ee, "Pale Fruit Pink");
        putSketch("E00", 0xfdf3ea, "Cotton Pearl");
        putSketch("E01", 0xffeee4, "Pink Flamingo");
        putSketch("E02", 0xfeece0, "Fruit Pink");
        putSketch("E04", 0xe4bcc4, "Lipstick Natural");
        putSketch("E07", 0xcc816a, "Light Mahogany");
        putSketch("E08", 0xca6553, "Brown");
        putSketch("E09", 0xd96a4f, "Burnt Sienna");
        putSketch("E11", 0xfee9d6, "Barley Beige");
        putSketch("E13", 0xe9c5af, "Light Suntan");
        putSketch("E15", 0xfbbb8d, "Dark Suntan");
        putSketch("E17", 0xb85f57, "Reddish Brass");
        putSketch("E18", 0x88534d, "Copper");
        putSketch("E19", 0xc45238, "Redwood");
        putSketch("E21", 0xfde2c7, "Soft Sun");
        putSketch("E23", 0xeccab1, "Hazelnut");
        putSketch("E25", 0xd2a482, "Caribe Cocoa");
        putSketch("E27", 0x997663, "Milk Chocolate");
        putSketch("E29", 0x884636, "Burnt Umber");
        putSketch("E30", 0xf7f0d6, "Bisque");
        putSketch("E31", 0xf2e6ce, "Brick Beige");
        putSketch("E33", 0xf3d2b1, "Sand");
        putSketch("E34", 0xf0caa6, "Toast");
        putSketch("E35", 0xe6c3a3, "Chamois");
        putSketch("E37", 0xcc9159, "Sepia");
        putSketch("E39", 0xc5743f, "Leather");
        putSketch("E40", 0xf2e8dc, "Brick White");
        putSketch("E41", 0xfef1e1, "Pearl White");
        putSketch("E42", 0xf3ead9, "Sand White");
        putSketch("E43", 0xe8dabd, "Dull Ivory");
        putSketch("E44", 0xc5b9a9, "Clay");
        putSketch("E47", 0x8a6e59, "Dark Brown");
        putSketch("E49", 0x634c3c, "Dark Bark");
        putSketch("E50", 0xf4ebf0, "Egg Shell");
        putSketch("E51", 0xfeecd6, "Milky White");
        putSketch("E53", 0xf3e6c3, "Raw Silk");
        putSketch("E55", 0xf1dfb9, "Light Camel");
        putSketch("E57", 0xb18558, "Light Walnut");
        putSketch("E59", 0x9a7f6c, "Walnut");
        putSketch("E70", 0xefeae6, "Ash Rose");
        putSketch("E71", 0xe2d7d3, "Champagne");
        putSketch("E74", 0xa1847c, "Cocoa Brown");
        putSketch("E77", 0x7f604e, "Maroon");
        putSketch("E79", 0x4a2c22, "Cashew");
        putSketch("E81", 0xf0e6c2, "Ivory");
        putSketch("E84", 0xae9f80, "- Sketch Khaki");
        putSketch("E87", 0x6f604d, "Fig");
        putSketch("E89", 0x5a4939, "- Sketch Pecan");
        putSketch("E93", 0xfed2b9, "Tea Rose");
        putSketch("E95", 0xfcbc7e, "Tea Orange");
        putSketch("E97", 0xed9c5d, "Deep Orange");
        putSketch("E99", 0xb46034, "Baked Clay");
        putSketch("FB2", 0x058fd0, "Fluorescent Dull Blue");
        putSketch("FBG2", 0x62cbe8, "Fluorescent Dull Blue Green");
        putSketch("FRV1", 0xf5a3c7, "Fluorescent Pink");
        putSketch("FV2", 0x7f74b6, "Fluorescent Dull Violet");
        putSketch("FY1", 0xfff697, "Fluorescent Yellow Orange");
        putSketch("FYG1", 0x9ecd43, "Fluorescent Yellow");
        putSketch("FYG2", 0x9ecd43, "Fluorescent Dull Yellow Green");
        putSketch("FYR1", 0xfecc99, "Fluorescent Orange");
        putSketch("G0000", 0xf1f7f3, "Crystal Opal");
        putSketch("G000", 0xeaf5ed, "Pale Green");
        putSketch("G00", 0xe3f2ed, "Jade Green");
        putSketch("G02", 0xcfe8d3, "Spectrum Green");
        putSketch("G03", 0xb6da9c, "Meadow Green");
        putSketch("G05", 0x69c07b, "Emerald Green");
        putSketch("G07", 0x7bc576, "Nile Green");
        putSketch("G09", 0x7ac465, "Veronese Green");
        putSketch("G12", 0xd2e8c4, "Sea Green");
        putSketch("G14", 0x97cf90, "Apple Green");
        putSketch("G16", 0x60c198, "Malachite");
        putSketch("G17", 0x14b37d, "Forest Green");
        putSketch("G19", 0x2db98a, "Bright Parrot Green");
        putSketch("G20", 0xedf6db, "Wax White");
        putSketch("G21", 0xc4e4cd, "Lime Green");
        putSketch("G24", 0xc3e0b4, "Willow");
        putSketch("G28", 0x119462, "Ocean Green");
        putSketch("G29", 0x197c5d, "Pine Tree Green");
        putSketch("G40", 0xe4f1df, "Dim Green");
        putSketch("G43", 0xd7e7a8, "Various Pistachio");
        putSketch("G46", 0x579e74, "Sketch Mistletoe");
        putSketch("G82", 0xccdab9, "Spring Dim Green");
        putSketch("G85", 0x9dc3aa, "Verdigris");
        putSketch("G94", 0x98a786, "Grayish Olive");
        putSketch("G99", 0x5f7e3a, "Olive");
        putSketch("N0", 0xeceeed, "Neutral Gray");
        putSketch("N10", 0x312f30, "Neutral Gray");
        putSketch("N1", 0xe2e3e5, "Neutral Gray");
        putSketch("N2", 0xdadbdd, "Neutral Gray");
        putSketch("N3", 0xd1d2d4, "Neutral Gray");
        putSketch("N4", 0xbcbdc1, "Neutral Gray");
        putSketch("N5", 0xa8a9ad, "Neutral Gray");
        putSketch("N6", 0x949599, "Neutral Gray");
        putSketch("N7", 0x77787c, "Neutral Gray");
        putSketch("N8", 0x636466, "Neutral Gray");
        putSketch("N9", 0x4c4d4f, "Neutral Gray");
        putSketch("R0000", 0xfef3ef, "Pink Beryl");
        putSketch("R000", 0xfef0e7, "Cherry White");
        putSketch("R00", 0xfeeae1, "Pinkish White");
        putSketch("R01", 0xfde0d8, "Pinkish Vanilla");
        putSketch("R02", 0xfdd3c7, "Rose Salmon");
        putSketch("R05", 0xf6927b, "Salmon Red");
        putSketch("R08", 0xf26754, "Vermilion");
        putSketch("R11", 0xfde1d5, "Pale Cherry Pink");
        putSketch("R12", 0xfcd3c1, "Light Tea Rose");
        putSketch("R14", 0xf59b92, "Light Rouge");
        putSketch("R17", 0xf4846c, "Lipstick Orange");
        putSketch("R20", 0xfcd7cf, "Blush");
        putSketch("R21", 0xfac1b6, "Sardonyx");
        putSketch("R22", 0xf8b7b1, "Light Prawn");
        putSketch("R24", 0xf27579, "Prawn");
        putSketch("R27", 0xf15062, "Cadmium Red");
        putSketch("R29", 0xed174b, "Lipstick Red");
        putSketch("R30", 0xfce3df, "Pale Yellowish Pink");
        putSketch("R32", 0xfac1ba, "Peach");
        putSketch("R35", 0xf27185, "Coral");
        putSketch("R37", 0xe86c74, "Carmine");
        putSketch("R39", 0xcb487a, "Garnet");
        putSketch("R43", 0xee848e, "Bougainvillaea");
        putSketch("R46", 0xe04d69, "Strong Red");
        putSketch("R56", 0xd27c95, "Currant");
        putSketch("R59", 0xb74f70, "Cardinal");
        putSketch("R81", 0xf1c8d6, "Rose Pink");
        putSketch("R83", 0xf19cb9, "Rose Mist");
        putSketch("R85", 0xd36a93, "Rose Red");
        putSketch("R89", 0x7d2b42, "Dark Red");
        putSketch("RV0000", 0xf2eaf5, "Evening Primrose");
        putSketch("RV000", 0xf4e2ee, "Pale Purple");
        putSketch("RV00", 0xf1daea, "Water Lily");
        putSketch("RV02", 0xfad5e6, "Sugared Almond Pink");
        putSketch("RV04", 0xf6a3bf, "Shock  Pink");
        putSketch("RV06", 0xf386af, "Cerise");
        putSketch("RV09", 0xe171ac, "Fuchsia");
        putSketch("RV10", 0xfdecf4, "Pale Pink");
        putSketch("RV11", 0xfbd6dd, "Pink");
        putSketch("RV13", 0xf9c9d7, "Tender Pink");
        putSketch("RV14", 0xf495b7, "Begonia Pink");
        putSketch("RV17", 0xdb7eb3, "Deep Magenta");
        putSketch("RV19", 0xd268aa, "Red Violet");
        putSketch("RV21", 0xfde8e7, "Light Pink");
        putSketch("RV23", 0xf8bac9, "Pure Pink");
        putSketch("RV25", 0xf493be, "Dog Rose Flower");
        putSketch("RV29", 0xef4880, "Crimson");
        putSketch("RV32", 0xfad3ce, "Shadow Pink");
        putSketch("RV34", 0xf9afae, "Dark Pink");
        putSketch("RV42", 0xf8bbb6, "Salmon Pink");
        putSketch("RV52", 0xf9cade, "Various Cotton Candy");
        putSketch("RV55", 0xe9a5ca, "Hollyhock");
        putSketch("RV63", 0xd09dae, "Begonia");
        putSketch("RV66", 0xb86a84, "Raspberry");
        putSketch("RV69", 0x8b576e, "Peony");
        putSketch("RV91", 0xe6d4e2, "Garyish Cherry");
        putSketch("RV93", 0xe7b6cc, "Smokey Purple");
        putSketch("RV95", 0xb684a1, "Baby Blossoms");
        putSketch("RV99", 0x5a4858, "Argyle Purple");
        putSketch("T0", 0xeceeed, "Toner Gray");
        putSketch("T10", 0x322e2d, "Toner Gray");
        putSketch("T1", 0xeaeae8, "Toner Gray");
        putSketch("T2", 0xe0e0de, "Toner Gray");
        putSketch("T3", 0xd1d2cc, "Toner Gray");
        putSketch("T4", 0xbcbbb9, "Toner Gray");
        putSketch("T5", 0xa8a7a3, "Toner Gray");
        putSketch("T6", 0x949590, "Toner Gray");
        putSketch("T7", 0x777674, "Toner Gray");
        putSketch("T8", 0x63645f, "Toner Gray");
        putSketch("T9", 0x4c4b49, "Toner Gray");
        putSketch("V0000", 0xf0edf6, "Rose Quartz");
        putSketch("V000", 0xe9e5f3, "Pale Heath");
        putSketch("V01", 0xe4c1d9, "Heath");
        putSketch("V04", 0xe6aace, "Lilac");
        putSketch("V05", 0xe2a6ca, "Azalea");
        putSketch("V06", 0xce95c2, "Lavender");
        putSketch("V09", 0x8754a1, "Violet");
        putSketch("V12", 0xeed7e9, "Pale Lilac");
        putSketch("V15", 0xd3a6cd, "Mallow");
        putSketch("V17", 0xa092c7, "Amethyst");
        putSketch("V20", 0xe2e0ed, "Wisteria");
        putSketch("V22", 0xb2b1d0, "Sketch Ash Lavender");
        putSketch("V25", 0x857fad, "Pale Blackberry");
        putSketch("V28", 0x6b668e, "Sketch Eggplant");
        putSketch("V91", 0xe8c4d0, "Pale Grape");
        putSketch("V93", 0xe5c1db, "Early Grape");
        putSketch("V95", 0xb77ca8, "Light Grape");
        putSketch("V99", 0x524358, "Aubergine");
        putSketch("W00", 0xf3f3eb, "Warm Gray");
        putSketch("W0", 0xecece4, "Warm Gray");
        putSketch("W10", 0x302f2b, "Warm Gray");
        putSketch("W1", 0xe7e7df, "Warm Gray No. 1");
        putSketch("W2", 0xddddd5, "Warm Gray");
        putSketch("W3", 0xd2d2ca, "Warm Gray No. 3");
        putSketch("W4", 0xbcbdb7, "Warm Gray");
        putSketch("W5", 0xa8a9a4, "Warm Gray No. 5");
        putSketch("W6", 0x94958f, "Warm Gray");
        putSketch("W7", 0x777873, "Warm Gray No. 7");
        putSketch("W8", 0x63645f, "Warm Gray");
        putSketch("W9", 0x4c4d48, "Warm Gray");
        putSketch("Y0000", 0xfefef4, "Yellow Fluorite");
        putSketch("Y000", 0xfffce9, "Pale Lemon");
        putSketch("Y00", 0xfefddf, "Barium Yellow");
        putSketch("Y02", 0xf6f396, "Canary Yellow");
        putSketch("Y04", 0xede556, "Acacia");
        putSketch("Y06", 0xfef56c, "Yellow");
        putSketch("Y08", 0xfef200, "Acid Yellow");
        putSketch("Y11", 0xfffbcc, "Pale Yellow");
        putSketch("Y13", 0xfbf7ae, "Lemon Yellow");
        putSketch("Y15", 0xfee96c, "Cadmium Yellow");
        putSketch("Y17", 0xffe455, "Golden Yellow");
        putSketch("Y18", 0xfeed55, "Lightning Yellow");
        putSketch("Y19", 0xffe93e, "Napoli Yellow");
        putSketch("Y21", 0xffeec2, "Buttercup Yellow");
        putSketch("Y23", 0xfbe3b3, "Yellowish Beige");
        putSketch("Y26", 0xf0dd67, "Mustard");
        putSketch("Y28", 0xcaa869, "Lionet Gold");
        putSketch("Y32", 0xf9dec0, "Cashmere");
        putSketch("Y35", 0xffd879, "Maize");
        putSketch("Y38", 0xffd374, "Honey");
        putSketch("YG0000", 0xf2f7e0, "Lily White");
        putSketch("YG00", 0xe6e69e, "Mimosa Yellow");
        putSketch("YG01", 0xe2ebb2, "Green Bice");
        putSketch("YG03", 0xdeeaaa, "Yellow Green");
        putSketch("YG05", 0xd6e592, "Salad");
        putSketch("YG06", 0xc4df92, "Yellowish Green");
        putSketch("YG07", 0xa5cf4f, "Acid Green");
        putSketch("YG09", 0x82c566, "Lettuce Green");
        putSketch("YG11", 0xe5f0d0, "Mignonette");
        putSketch("YG13", 0xd4e59f, "Chartreuse");
        putSketch("YG17", 0x72c156, "Grass Green");
        putSketch("YG21", 0xf7f6be, "Anise");
        putSketch("YG23", 0xe6eb8f, "New Leaf");
        putSketch("YG25", 0xd0e17b, "Celadon Green");
        putSketch("YG41", 0xd5ebd4, "Pale Cobalt Green");
        putSketch("YG45", 0xb4dcb7, "Cobalt Green");
        putSketch("YG61", 0xd6e9d6, "Pale Moss");
        putSketch("YG63", 0xa0caa2, "Pea Green");
        putSketch("YG67", 0x81bf8c, "Moss");
        putSketch("YG91", 0xdad7ae, "Putty");
        putSketch("YG93", 0xd2d29c, "Grayish Yellow");
        putSketch("YG95", 0xcbc65e, "Pale Olive");
        putSketch("YG97", 0x958f03, "Spanish Olive");
        putSketch("YG99", 0x4e6a15, "Marine Green");
        putSketch("YR0000", 0xfff3e5, "Pale Chiffon");
        putSketch("YR000", 0xfeecd8, "Silk");
        putSketch("YR00", 0xfed6bd, "Powder Pink");
        putSketch("YR01", 0xfedac2, "Peach Puff");
        putSketch("YR02", 0xfcdcc5, "Light Orange");
        putSketch("YR04", 0xfec369, "Chrome Orange");
        putSketch("YR07", 0xf26f39, "Cadmium Orange");
        putSketch("YR09", 0xf15524, "Chinese Orange");
        putSketch("YR12", 0xffe2a6, "Loquat");
        putSketch("YR14", 0xfec84e, "Caramel");
        putSketch("YR15", 0xfbb884, "Pumpkin Yellow");
        putSketch("YR16", 0xfeb729, "Apricot");
        putSketch("YR18", 0xf26b3c, "Sanguine");
        putSketch("YR20", 0xffe1bf, "Yellowish Shade");
        putSketch("YR21", 0xf5ddb1, "Cream");
        putSketch("YR23", 0xeccf8b, "Yellow Ochre");
        putSketch("YR24", 0xf0cf64, "Pale Sepia");
        putSketch("YR27", 0xd56638, "Tuscan Orange");
        putSketch("YR30", 0xfef2da, "Macadamia Nut");
        putSketch("YR31", 0xffdea8, "Light Reddish Yellow");
        putSketch("YR61", 0xfddac4, "Spring Orange");
        putSketch("YR65", 0xfaae60, "Atoll");
        putSketch("YR68", 0xf37022, "Orange");
        putSketch("YR82", 0xfdc68d, "Mellow Peach");

        // 18 Standard Copic Sets from Drawbot_image_to_gcode_v2.pde
        PRESETS.add(new MarkerSet("Dark Greys", "100", "N10", "N8", "N6", "N4", "N2"));
        PRESETS.add(new MarkerSet("Light Greys", "100", "100", "N7", "N5", "N3", "N2"));
        PRESETS.add(new MarkerSet("Warm Greys", "100", "W10", "W8", "W6", "W4", "W2"));
        PRESETS.add(new MarkerSet("Cool Greys", "100", "C10", "C8", "C6", "C4", "C2"));
        PRESETS.add(new MarkerSet("Mixed Greys 1", "100", "100", "C7", "W5", "C3", "W2"));
        PRESETS.add(new MarkerSet("Mixed Greys 2", "100", "100", "W7", "C5", "W3", "C2"));
        PRESETS.add(new MarkerSet("Browns 1", "100", "100", "E49", "E27", "E13", "E00"));
        PRESETS.add(new MarkerSet("Dark Grey Browns 1", "100", "100", "E49", "E27", "E13", "N2"));
        PRESETS.add(new MarkerSet("Browns 2", "100", "100", "E49", "E27", "N4", "N2"));
        PRESETS.add(new MarkerSet("Dark Grey Browns 2", "100", "100", "E49", "N6", "N4", "N2"));
        PRESETS.add(new MarkerSet("Dark Grey Blues", "100", "100", "B37", "N6", "N4", "N2"));
        PRESETS.add(new MarkerSet("Dark Grey Red", "100", "100", "R59", "N6", "N4", "N2"));
        PRESETS.add(new MarkerSet("Dark Grey Violet", "100", "100", "G29", "N6", "N4", "N2"));
        PRESETS.add(new MarkerSet("Dark Grey Orange", "100", "100", "YR09", "N6", "N4", "N2"));
        PRESETS.add(new MarkerSet("Blue Green", "100", "100", "B39", "G28", "B26", "G14"));
        PRESETS.add(new MarkerSet("Purples", "100", "100", "B39", "V09", "B02", "V04"));
        PRESETS.add(new MarkerSet("Reds", "100", "100", "R29", "R27", "R24", "R20"));
        PRESETS.add(new MarkerSet("Yellow Green", "100", "E29", "YG99", "Y17", "YG03", "Y11"));
    }

    private static void putOriginal(String code, int rgb, String name) {
        ORIGINAL_MARKERS.put(code, new Marker(code, rgb, name));
    }

    private static void putSketch(String code, int rgb, String name) {
        SKETCH_MARKERS.put(code, new Marker(code, rgb, name));
    }

    public static Marker getOriginalMarker(String code) {
        Marker m = ORIGINAL_MARKERS.get(code);
        if (m == null) {
            // Fallback for code "100" (Black)
            return new Marker(code, 0x000000, "Black");
        }
        return m;
    }

    public static Color getOriginalColor(String code) {
        return getOriginalMarker(code).toColor();
    }

    public static int getOriginalRgb(String code) {
        return getOriginalMarker(code).rgb;
    }

    public static Marker getSketchMarker(String code) {
        Marker m = SKETCH_MARKERS.get(code);
        if (m == null) {
            return getOriginalMarker(code);
        }
        return m;
    }

    public static Collection<Marker> getAllOriginalMarkers() {
        return Collections.unmodifiableCollection(ORIGINAL_MARKERS.values());
    }

    /**
     * Finds the closest Copic marker in the original catalog using the weighted Euclidean distance
     * formula from Copic.pde:
     *   d = (r2-r1)^2 * 0.30 + (g2-g1)^2 * 0.59 + (b2-b1)^2 * 0.11
     */
    public static String getClosestOriginal(int rgb) {
        int r1 = (rgb >> 16) & 0xFF;
        int g1 = (rgb >> 8) & 0xFF;
        int b1 = rgb & 0xFF;

        double closestDist = Double.MAX_VALUE;
        String closestCode = "100";

        for (Marker m : ORIGINAL_MARKERS.values()) {
            int r2 = (m.rgb >> 16) & 0xFF;
            int g2 = (m.rgb >> 8) & 0xFF;
            int b2 = m.rgb & 0xFF;

            double dr = (r2 - r1) * 0.30;
            double dg = (g2 - g1) * 0.59;
            double db = (b2 - b1) * 0.11;
            double dist = dr * dr + dg * dg + db * db;

            if (dist < closestDist) {
                closestDist = dist;
                closestCode = m.code;
            }
        }
        return closestCode;
    }
}
