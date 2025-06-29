public class EnglishNamesTest {
    public static void main(String[] args) {
        // 动物名称数组 (40个)
        String[] animals = {
                "Lion", "Tiger", "Elephant", "Giraffe", "Zebra",
                "Kangaroo", "Koala", "Panda", "Hippopotamus", "Rhino",
                "Crocodile", "Gorilla", "Chimpanzee", "Leopard", "Cheetah",
                "Wolf", "Fox", "Bear", "Polar Bear", "Penguin",
                "Dolphin", "Shark", "Whale", "Octopus", "Eagle",
                "Owl", "Parrot", "Peacock", "Swan", "Flamingo",
                "Butterfly", "Bee", "Ant", "Spider", "Scorpion",
                "Horse", "Cow", "Sheep", "Goat", "Rabbit"
        };

        // 植物名称数组 (35个)
        String[] plants = {
                "Oak", "Pine", "Maple", "Willow", "Palm",
                "Bamboo", "Cactus", "Fern", "Moss", "Ivy",
                "Rose", "Tulip", "Sunflower", "Orchid", "Daisy",
                "Lily", "Lavender", "Jasmine", "Carnation", "Daffodil",
                "Cedar", "Sequoia", "Redwood", "Birch", "Cherry Blossom",
                "Baobab", "Bonsai", "Cypress", "Eucalyptus", "Ficus",
                "Magnolia", "Olive", "Sakura", "Spruce", "Wisteria"
        };

        // 水果名称数组 (35个)
        String[] fruits = {
                "Apple", "Banana", "Orange", "Grape", "Strawberry",
                "Watermelon", "Pineapple", "Mango", "Kiwi", "Peach",
                "Pear", "Cherry", "Plum", "Lemon", "Lime",
                "Coconut", "Papaya", "Avocado", "Pomegranate", "Fig",
                "Blueberry", "Raspberry", "Blackberry", "Cranberry", "Apricot",
                "Guava", "Passion Fruit", "Dragon Fruit", "Lychee", "Melon",
                "Cantaloupe", "Tangerine", "Grapefruit", "Kumquat", "Persimmon"
        };

        // 打印验证总数
        int total = animals.length + plants.length + fruits.length;
        System.out.println("动物总数: " + animals.length);
        System.out.println("植物总数: " + plants.length);
        System.out.println("水果总数: " + fruits.length);
        System.out.println("名称总数: " + total + " (>100)");
    }
}