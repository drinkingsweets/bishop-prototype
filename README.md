### Эндпоинты
- POST /command/create — создать команду (COMMON / CRITICAL)

- GET /command/metrics — метрики (размер очереди, выполненные)

### Запуск
```bash
./gradlew bootRun
```

🧪 Тесты
```bash
./gradlew test
```
📝 Пример команды

```json
{
"description": "тест",
"priority": "COMMON",
"author": "mvl",
"time": "2025-07-20T14:30:00"
}
```
