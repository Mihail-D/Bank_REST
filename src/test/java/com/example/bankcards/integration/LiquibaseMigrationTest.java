package com.example.bankcards.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test-db")
@Transactional
class LiquibaseMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldHaveAllRequiredTables() {
        String query = """
            SELECT table_name 
            FROM information_schema.tables 
            WHERE table_schema = 'public' 
            AND table_type = 'BASE TABLE'
            ORDER BY table_name
            """;

        List<String> tables = jdbcTemplate.queryForList(query, String.class);

        assertThat(tables).contains(
            "users",
            "cards",
            "transfers",
            "history",
            "databasechangelog",
            "databasechangeloglock"
        );
    }

    @Test
    void shouldHaveCorrectUsersTableStructure() {
        String query = """
            SELECT column_name, data_type, is_nullable, column_default
            FROM information_schema.columns
            WHERE table_name = 'users'
            ORDER BY ordinal_position
            """;

        List<Map<String, Object>> columns = jdbcTemplate.queryForList(query);

        assertThat(columns).hasSize(7); // Изменил с 6 на 7 из-за добавления поля active

        assertThat(columns.stream().anyMatch(col -> "id".equals(col.get("column_name")))).isTrue();
        assertThat(columns.stream().anyMatch(col -> "name".equals(col.get("column_name")))).isTrue();
        assertThat(columns.stream().anyMatch(col -> "username".equals(col.get("column_name")))).isTrue();
        assertThat(columns.stream().anyMatch(col -> "email".equals(col.get("column_name")))).isTrue();
        assertThat(columns.stream().anyMatch(col -> "password".equals(col.get("column_name")))).isTrue();
        assertThat(columns.stream().anyMatch(col -> "role".equals(col.get("column_name")))).isTrue();
        assertThat(columns.stream().anyMatch(col -> "active".equals(col.get("column_name")))).isTrue(); // Добавил проверку поля active
    }

    @Test
    void shouldHaveCorrectCardsTableStructure() {
        String query = """
            SELECT column_name, data_type, is_nullable
            FROM information_schema.columns
            WHERE table_name = 'cards'
            ORDER BY ordinal_position
            """;

        List<Map<String, Object>> columns = jdbcTemplate.queryForList(query);

        assertThat(columns).hasSize(6); // Было 5, стало 6 из-за balance
        assertThat(columns.stream().anyMatch(col -> "id".equals(col.get("column_name")))).isTrue();
        assertThat(columns.stream().anyMatch(col -> "encrypted_number".equals(col.get("column_name")))).isTrue();
        assertThat(columns.stream().anyMatch(col -> "status".equals(col.get("column_name")))).isTrue();
        assertThat(columns.stream().anyMatch(col -> "expiration_date".equals(col.get("column_name")))).isTrue();
        assertThat(columns.stream().anyMatch(col -> "balance".equals(col.get("column_name")))).isTrue(); // Новое поле
        assertThat(columns.stream().anyMatch(col -> "user_id".equals(col.get("column_name")))).isTrue();
    }

    @Test
    void shouldHaveCorrectTransfersTableStructure() {
        String query = """
            SELECT column_name, data_type, is_nullable
            FROM information_schema.columns
            WHERE table_name = 'transfers'
            ORDER BY ordinal_position
            """;

        List<Map<String, Object>> columns = jdbcTemplate.queryForList(query);

        assertThat(columns).hasSize(6);
        assertThat(columns.stream().anyMatch(col -> "id".equals(col.get("column_name")))).isTrue();
        assertThat(columns.stream().anyMatch(col -> "amount".equals(col.get("column_name")))).isTrue();
        assertThat(columns.stream().anyMatch(col -> "transfer_date".equals(col.get("column_name")))).isTrue();
        assertThat(columns.stream().anyMatch(col -> "from_card_id".equals(col.get("column_name")))).isTrue();
        assertThat(columns.stream().anyMatch(col -> "to_card_id".equals(col.get("column_name")))).isTrue();
        assertThat(columns.stream().anyMatch(col -> "status".equals(col.get("column_name")))).isTrue();
    }

    @Test
    void shouldHaveCorrectHistoryTableStructure() {
        String query = """
            SELECT column_name, data_type, is_nullable
            FROM information_schema.columns
            WHERE table_name = 'history'
            ORDER BY ordinal_position
            """;

        List<Map<String, Object>> columns = jdbcTemplate.queryForList(query);

        assertThat(columns).hasSize(7);
        assertThat(columns.stream().anyMatch(col -> "id".equals(col.get("column_name")))).isTrue();
        assertThat(columns.stream().anyMatch(col -> "event_type".equals(col.get("column_name")))).isTrue();
        assertThat(columns.stream().anyMatch(col -> "event_date".equals(col.get("column_name")))).isTrue();
        assertThat(columns.stream().anyMatch(col -> "description".equals(col.get("column_name")))).isTrue();
        assertThat(columns.stream().anyMatch(col -> "user_id".equals(col.get("column_name")))).isTrue();
        assertThat(columns.stream().anyMatch(col -> "card_id".equals(col.get("column_name")))).isTrue();
        assertThat(columns.stream().anyMatch(col -> "transfer_id".equals(col.get("column_name")))).isTrue();
    }

    @Test
    void shouldHaveCorrectForeignKeys() {
        String query = """
            SELECT 
                tc.constraint_name,
                tc.table_name,
                kcu.column_name,
                ccu.table_name AS foreign_table_name,
                ccu.column_name AS foreign_column_name
            FROM information_schema.table_constraints AS tc
            JOIN information_schema.key_column_usage AS kcu
                ON tc.constraint_name = kcu.constraint_name
            JOIN information_schema.constraint_column_usage AS ccu
                ON ccu.constraint_name = tc.constraint_name
            WHERE tc.constraint_type = 'FOREIGN KEY'
            AND tc.table_schema = 'public'
            ORDER BY tc.table_name, tc.constraint_name
            """;

        List<Map<String, Object>> foreignKeys = jdbcTemplate.queryForList(query);

        assertThat(foreignKeys).isNotEmpty();

        assertThat(foreignKeys.stream().anyMatch(fk ->
            "cards".equals(fk.get("table_name")) &&
            "user_id".equals(fk.get("column_name")) &&
            "users".equals(fk.get("foreign_table_name"))
        )).isTrue();

        boolean hasTransactionsFk = foreignKeys.stream().anyMatch(fk ->
            "transactions".equals(fk.get("table_name")) &&
            ("from_card_id".equals(fk.get("column_name")) || "to_card_id".equals(fk.get("column_name"))) &&
            "cards".equals(fk.get("foreign_table_name"))
        );

        boolean hasTransfersFk = foreignKeys.stream().anyMatch(fk ->
            "transfers".equals(fk.get("table_name")) &&
            ("from_card_id".equals(fk.get("column_name")) || "to_card_id".equals(fk.get("column_name"))) &&
            "cards".equals(fk.get("foreign_table_name"))
        );

        assertThat(hasTransactionsFk || hasTransfersFk).isTrue();

        assertThat(foreignKeys.stream().anyMatch(fk ->
            "history".equals(fk.get("table_name")) &&
            "user_id".equals(fk.get("column_name")) &&
            "users".equals(fk.get("foreign_table_name"))
        )).isTrue();

        assertThat(foreignKeys.stream().anyMatch(fk ->
            "history".equals(fk.get("table_name")) &&
            "card_id".equals(fk.get("column_name")) &&
            "cards".equals(fk.get("foreign_table_name"))
        )).isTrue();
    }

    @Test
    void shouldHaveLiquibaseChangelogEntries() {
        String query = """
            SELECT id, author, filename, dateexecuted, orderexecuted 
            FROM databasechangelog 
            ORDER BY orderexecuted
            """;

        List<Map<String, Object>> changesets = jdbcTemplate.queryForList(query);

        assertThat(changesets).isNotEmpty();
        assertThat(changesets.stream().anyMatch(cs ->
            "01-create-users-table".equals(cs.get("id"))
        )).isTrue();
        assertThat(changesets.stream().anyMatch(cs ->
            "02-create-cards-table".equals(cs.get("id"))
        )).isTrue();
        assertThat(changesets.stream().anyMatch(cs ->
            "05-create-transfers-table-new".equals(cs.get("id"))
        )).isTrue();
        assertThat(changesets.stream().anyMatch(cs ->
            "06-create-history-table-new".equals(cs.get("id"))
        )).isTrue();
    }
}
