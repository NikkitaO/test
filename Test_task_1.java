import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;




public class DocumentManager {
    // Сховище документів у пам'яті
    private final Map<String, Document> documents = new HashMap<>();
    // Лічильник для генерації id
    private long idCounter = 1;

    /**
     * Зберігає або оновлює документ у сховищі.
     * Генерує id та встановлює час створення для нових документів.
     */
    public Document save(Document document) {
        if (document.getId() == null) {
            document.setId(generateId());
        }
        if (document.getCreated() == null) {
            document.setCreated(Instant.now());
        }
        documents.put(document.getId(), document);
        return document;
    }

    /**
     * Пошук документів за заданими критеріями.
     * Використовує потоки для фільтрації документів за кожним критерієм.
     */
    public List<Document> search(SearchRequest request) {
        return documents.values().stream()
                .filter(doc -> matchesTitlePrefixes(doc, request.getTitlePrefixes()))
                .filter(doc -> matchesContainsContents(doc, request.getContainsContents()))
                .filter(doc -> matchesAuthorIds(doc, request.getAuthorIds()))
                .filter(doc -> matchesCreatedRange(doc, request.getCreatedFrom(), request.getCreatedTo()))
                .collect(Collectors.toList());
    }

    /**
     * Пошук документа за id.
     * Повертає Optional для обробки випадку, коли документ не знайдено.
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(documents.get(id));
    }

    // Генерація унікального id
    private String generateId() {
        return String.valueOf(idCounter++);
    }

    // Перевірка відповідності заголовка документа заданим префіксам
    private boolean matchesTitlePrefixes(Document doc, List<String> titlePrefixes) {
        return titlePrefixes == null || titlePrefixes.isEmpty() ||
                titlePrefixes.stream().anyMatch(prefix -> doc.getTitle().startsWith(prefix));
    }

    // Перевірка наявності всіх вказаних підрядків у вмісті документа
    private boolean matchesContainsContents(Document doc, List<String> containsContents) {
        return containsContents == null || containsContents.isEmpty() ||
                containsContents.stream().allMatch(content -> doc.getContent().contains(content));
    }

    // Перевірка відповідності id автора документа заданим id
    private boolean matchesAuthorIds(Document doc, List<String> authorIds) {
        return authorIds == null || authorIds.isEmpty() ||
                authorIds.contains(doc.getAuthor().getId());
    }

    // Перевірка дати створення документа у діапазон
    private boolean matchesCreatedRange(Document doc, Instant createdFrom, Instant createdTo) {
        return (createdFrom == null || !doc.getCreated().isBefore(createdFrom)) &&
                (createdTo == null || !doc.getCreated().isAfter(createdTo));
    }

    // Класи даних
    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
