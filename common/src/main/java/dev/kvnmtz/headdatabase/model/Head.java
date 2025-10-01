package dev.kvnmtz.headdatabase.model;

import java.util.List;

public record Head(String name, String texture, String category, List<String> tags) {

    @Override
    public String toString() {
        return "Head{" +
                "name='" + name + '\'' +
                ", texture='" + texture + '\'' +
                ", category='" + category + '\'' +
                ", tags=" + tags +
                '}';
    }
}