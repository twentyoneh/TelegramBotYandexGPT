Телеграм бот для обучения


Пользователь отправляет в Telegram-бот сообщение с вопросом или ключевыми словами, связанными с учебными материалами. Бот обрабатывает запрос с помощью AI модели (например: YandexGPT, Mistral, Claude, LLaMA) и формирует релевантный ответ.

В ответе бот дает краткое пояснение и предоставляет ссылку на соответствующий раздел курса или конкретный материал.

В рамках данной задачи в качестве учебного материала необходимо использовать предоставленный pdf-файл

**Бот умеет:**

- Подсказывать, где найти определённую тему;
- Давать ссылки на материалы по ключевым словам;
- Отвечать на базовые вопросы, если информация есть в материалах.

## Технологии:
<p>
  <img src="https://img.shields.io/badge/Java-21+-orange?logo=openjdk" />
  <img src="https://img.shields.io/badge/Spring%20Framework-5.x-6DB33F?logo=spring&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Hibernate-5.x-59666C?logo=hibernate&logoColor=white" />
  <img src="https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-15+-336791?logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Liquibase-4.x-2962FF?logo=liquibase&logoColor=white" />
  <img src="https://https://img.shields.io/swagger/valid/3.0" />
</p>

**Backend задачи:**

- Реализовать REST API для взаимодействия с Telegram-ботом и frontend:
    - Обработка пользовательских запросов.
    - Поиск ответа на основе найденных материалов.
    - Выдача ссылок на первоисточник.
- Реализовать API для загрузки и удаления материалов.
- Настроить логирование запросов, ответов и ошибок.
- Обеспечить покрытие кода тестами не менее 50%.
- Обеспечить безопасность доступа (например, базовая авторизация или JWT).
- Реализовать возможность обновления базы без перезапуска сервиса.