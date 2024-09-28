    package org.example.entity;

    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Post {
        private Integer id;
        private Integer userId;
        private String title;
        private String body;
        private String comments;
    }
