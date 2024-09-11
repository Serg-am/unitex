import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONObject;

public class Main extends JFrame {
    private JTextArea logArea; // Область для логов запросов и ответов
    private JTextField filterField; // Поле для фильтрации данных в таблице
    private JTable table; // Таблица для отображения данных
    private DefaultTableModel tableModel; // Модель данных для таблицы
    private HttpClient httpClient;



    // Конструктор основного окна приложения
    public Main() {
        setTitle("Swing Echo Сервис приложение");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        httpClient = HttpClient.newHttpClient();

        JTabbedPane tabbedPane = new JTabbedPane();

        // Вкладка для логов
        JPanel logPanel = new JPanel(new BorderLayout());
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logPanel.add(logScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("Log", logPanel);

        // Вкладка для таблицы с данными
        JPanel tablePanel = new JPanel(new BorderLayout());
        filterField = new JTextField();
        tablePanel.add(filterField, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Param", "Value"}, 0);
        table = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(table);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        tabbedPane.addTab("Table", tablePanel);

        add(tabbedPane); // Добавляем панель с вкладками в окно

        loadData(); // Загружаем данные

        addFilterFunctionality(); // Добавляем функционал фильтрации для таблицы
    }


    //Метод для загрузки данных с echo сервиса
    private void loadData() {
        String url = "https://postman-echo.com/get?param1=value1&param2=value2";
        log("Отправка запроса: " + url);

        // Формируем HTTP запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        // Отправляем запрос
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::handleResponse)
                .exceptionally(e -> {
                    log("Ошибка: " + e.getMessage());
                    return null;
                });
    }


    // Обработка ответа от echo сервиса
    private void handleResponse(String response) {
        log("Ответ: " + response);

        JSONObject jsonObject = new JSONObject(response);
        JSONObject args = jsonObject.getJSONObject("args");

        tableModel.setRowCount(0);

        for (String key : args.keySet()) {
            tableModel.addRow(new Object[]{key, args.getString(key)});
        }
    }

    // Фильтрация текста
    private void addFilterFunctionality() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }

            private void applyFilter() {
                String filterText = filterField.getText();
                if (filterText.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filterText));
                }
            }
        });
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
        });
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main mainApp = new Main();
            mainApp.setVisible(true);
        });
    }
}
