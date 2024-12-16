import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class ChatGPTBot extends TelegramLongPollingBot {

    private final String openAiApiKey = "your_openai_api_key"; // Ваш API-ключ OpenAI
    private final String telegramBotToken = ""; // Токен Telegram-бота
    private static final Logger logger = Logger.getLogger(ChatGPTBot.class.getName());
    private Map<Long, String> users = new HashMap<>();
    private Map<Long, Long> testUsers = new HashMap<>();
    private final Map<Long, UserStates> userStates = new HashMap<>();
    UserStates userState;


    @Override
    public String getBotToken() {
        return telegramBotToken;
    }

    @Override
    public String getBotUsername() {
        return "margo_psyhelp_bot";  // Укажите username вашего бота
    }

    /* Метод для получения ответа от OpenAI
    public String getChatGPTResponse(String prompt) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String apiUrl = "https://api.openai.com/v1/completions";

            // Создаем JSON с запросом к API OpenAI
            JsonObject json = new JsonObject();
            json.addProperty("model", "text-davinci-003");  // Модель ChatGPT
            json.addProperty("prompt", prompt);
            json.addProperty("max_tokens", 150);  // Ограничение на количество слов

            // Создаем HTTP-запрос
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();

            // Отправляем запрос и получаем ответ
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Парсим ответ JSON и извлекаем текст
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            return jsonResponse
                    .getAsJsonArray("choices")
                    .get(0)
                    .getAsJsonObject()
                    .get("text")
                    .getAsString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "Извините, произошла ошибка при запросе к ChatGPT.";
        }
    }

     */

    // Метод обработки сообщений от пользователя
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText() || update.hasCallbackQuery()) {
            String userMessage;
            //String chatGPTResponse = getChatGPTResponse(userMessage);
            SendMessage message = new SendMessage();
            String chatId;


            if (update.hasMessage()) {
                userMessage = update.getMessage().getText();
                chatId = update.getMessage().getChatId().toString();

                logger.info("Message");
            } else if (update.hasCallbackQuery()) {
                userMessage = update.getCallbackQuery().getData();
                chatId = update.getCallbackQuery().getMessage().getChatId().toString();
                logger.info("Button");
            } else {
                SendMessage message1 = new SendMessage();
                message1.setText("Произошла ошибка при попытке прочитать ваше сообщение. Пожалуйста, отправьте его ещё раз");
                sendMessage(message1);
                logger.warning("Ошибка");
                return;
            }
            message.setChatId(chatId);
            userState = userStates.getOrDefault(chatId, UserStates.ASK_NAME);



            switch (userMessage) {
                case "/start":
                    start(chatId);
                    break;
                case "/tell":
                    tell(chatId);
                    break;
                case "/new_payment":
                    addPayment(chatId);
                    break;
                case "/to_pay":
                    payload(chatId);
                    break;
                case "/talk":
                    talk(chatId);
                    break;
                case "/aboutMargo":
                    aboutMargo(chatId);
                    break;
                case "/products":
                    // products(chatId);
                    break;
                case "/test":
                    test(chatId);
                    break;
                case "/rate":
                    rateYourMood(chatId);
                    break;
                case "/menu":
                    printMenu(chatId);
                    break;
                case "/roulette":
                    spin(chatId);
                    break;
                case "/getPrize":
                    getPrize(chatId);
                    break;
                case "/fillForm":
                    fillForm(chatId, userState);
                    break;


                    // Отправляем ответ обратно пользователю


            }
            if (!userState.equals(UserStates.DONE)) {
                fillForm(chatId, userState);
            }
            if (userMessage.contains("хорошо")) {
                areYouGood("хорошо", chatId);
            } else if (userMessage.contains("расстроен") || userMessage.contains("расстроена")) {
                areYouGood("плохо", chatId);
            } else if (userMessage.contains("mood")) {
                whatToDo(chatId);
            } else if (userMessage.contains("techTo")) {
                showTechniques(chatId, String.valueOf(message));
            } else {
                SendMessage message1 = new SendMessage();
                message1.setText("Я не понимаю твоего сообщения. Пожалуйста, попробуй сказать что-нибудь другое");
                sendMessage(message1);
                return;
            }
            try {
                execute(message);  // Отправка сообщения
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void printMenu(String chatId) {
        //проверка на наличие в базе
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText("Техническое сообщение: меню появится только если подписка оплачена. Иначе будет сообщение: " +
                "Готова продолжить нашу дружбу и стать членом прекрасного комьюнити?\uD83D\uDE3B\n" +
                "Жми \"оплатить подписку\". \n" +
                "\n" +
                "Ты выберешь мой уникальный аватар, мы сможем общаться каждый день и улучшать твоё состояние✨");
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Меню:");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        InlineKeyboardButton button4 = new InlineKeyboardButton();
        InlineKeyboardButton button5 = new InlineKeyboardButton();
        InlineKeyboardButton button6 = new InlineKeyboardButton();
        InlineKeyboardButton button7 = new InlineKeyboardButton();

        button1.setText("Поговорить или попросить совета");
        button1.setCallbackData("/chat");
        button2.setText("Волшебная рулетка");
        button2.setCallbackData("/roulette");
        button3.setText("О Марго");
        button3.setCallbackData("/aboutMargo");
        button4.setText("Продукты");
        button4.setCallbackData("/products");
        button5.setText("Оплата");
        button5.setCallbackData("/new_payment");
        button6.setText("Послание дня");
        button6.setCallbackData("/dayMessage");
        button7.setText("Вопрос дня");
        button7.setCallbackData("/dayQuestion");
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        List<InlineKeyboardButton> row4 = new ArrayList<>();
        List<InlineKeyboardButton> row5 = new ArrayList<>();
        List<InlineKeyboardButton> row6 = new ArrayList<>();
        List<InlineKeyboardButton> row7 = new ArrayList<>();
        row1.add(button1);
        row2.add(button2);
        row3.add(button3);
        row4.add(button4);
        row5.add(button5);
        row6.add(button6);
        row7.add(button7);

        buttons.add(row1);
        buttons.add(row2);
        buttons.add(row3);
        buttons.add(row4);
        buttons.add(row5);
        buttons.add(row6);
        buttons.add(row7);
        markup.setKeyboard((buttons));
        message.setReplyMarkup(markup);
        sendMessage(message);

        SendMessage message1 = new SendMessage();
        message1.setChatId(chatId);
        message1.setText("Техническое сообщение: далее сообщение появится, если это была первая оплата и человек " +
                "не заполнял анкету" +
                "\n\nРада видеть тебя в нашем уютном гнездышке\uD83E\uDE75\n" +
                "\n" +
                "\uD83C\uDF3FДержи ссылку на канал:\n" +
                "\n" +
                "https://t.me/+Wqx5enqC8IQ1Y2M6\n");
        InlineKeyboardMarkup markup2 = new InlineKeyboardMarkup();
        InlineKeyboardButton button21 = new InlineKeyboardButton();
        button21.setText("Давай познакомимся!");
        button21.setCallbackData("/fillForm");
        List<List<InlineKeyboardButton>> buttons2 = new ArrayList<>();
        List<InlineKeyboardButton> row21 = new ArrayList<>();
        row21.add(button21);
        buttons2.add(row21);
        markup2.setKeyboard((buttons2));
        message1.setReplyMarkup(markup2);
        sendMessage(message1);
    }

    private void fillForm(String chatId, UserStates userState) {


        switch (userState) {
            case ASK_NAME:
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText("А теперь я хотела бы познакомиться с тобой. Как тебя зовут? ");
                userStates.put(Long.valueOf(chatId), UserStates.ASK_AGE); // Переходим к следующему шагу
                break;

            case ASK_AGE:
                sendMessage(chatId, "Сколько тебе лет?");
                userStates.put(chatId, UserState.DONE); // Переходим к следующему шагу
                break;

            case DONE:
                String name = text; // Предполагаем, что имя было введено на первом шаге
                String age = text;  // Предполагаем, что возраст был введен на втором шаге
                sendMessage(chatId, "Приятно познакомиться, " + name + "! Тебе " + age + " лет.");
                userStates.remove(chatId); // Сбрасываем состояние
                break;
        }
    }
    private void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void start(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        message.setText("Каждый день мы будем общаться с тобой. " +
                "Я стану твоим верным другом и помощником по гармонизации " +
                "ментального состояния. В любой момент ты сможешь обратиться ко мне " +
                "за советом, помощью и поддержкой. Также я всегда буду тебя мотивировать " +
                "и вдохновлять. У меня всегда будут для тебя приятные подарки и " +
                "послания на день. А ещё со мной уютно и тепло, тут царит волшебная атмосфера");
        sendMessage(message);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        InlineKeyboardButton button = new InlineKeyboardButton();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        button.setText("Расскажи!");
        button.setCallbackData("/tell");
        row1.add(button);
        buttons.add(row1);
        markup.setKeyboard((buttons));

        SendMessage newMessage = new SendMessage();
        newMessage.setChatId(chatId);

        newMessage.setReplyMarkup(markup);
        newMessage.setText("Помимо общения со мной, тебя ждёт прекрасное комьюнити на канале. Я и канал – одно целое. \n" +
                "Рассказать подробнее про канал?");

        sendMessage(newMessage);
    }

    private void tell(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("\uD83E\uDD8BНа канале ты сможешь:\n" +
                "\n" +
                "♡ найти знакомство с единомышленниками \n" +
                "♡ уникальные, разработанные лично Марго, техники и практики на разные темы и сферы жизни \n" +
                "♡ личные встречи и тренинги \n" +
                "♡ ценные подкасты от Марго, которые помогут преобразить твою жизнь, трансформировать в лучшую сторону \n" +
                "♡ розыгрыши уникальных призов, в том числе авторская колода метафорических карт для проработки глубоких запросов и скидка на индивидуальную программу и многое другое \n" +
                "♡поддержка, общение и связь лично от Марго");
        sendMessage(message);
        SendMessage message1 = new SendMessage();
        message1.setChatId(chatId);

        message1.setText("Что ты получишь, войдя в канал?\uD83E\uDD70\n" +
                "\n" +
                "♡ улучшение состояния \n" +
                "♡ отсутствие тревоги и апатии \n" +
                "♡ подружишься со своими страхами \n" +
                "♡ исцелишь свои травмы \n" +
                "♡ ощутишь истинную свободу \n" +
                "♡ сбросишь с себя все ограничения и ментальные оковы \n" +
                "♡ узнаешь новые грани себя \n" +
                "♡ высвободишь истинное Я своей личности \n" +
                "♡ встанешь на свой жизненный путь, сбросив маски \n" +
                "♡ устранишь чувство одиночества \n" +
                "♡ полюбишь себя");

        sendMessage(message1);

        SendMessage message2 = new SendMessage();
        message2.setChatId(chatId);
        message2.setText("Я могу назвать ещё много всего, но всю ценность и исцеляющую силу невозможно уложить в один " +
                "текст, её нужно прочувствовать на себе\uD83C\uDF3F\uD83E\uDDF8\n" +
                "\n" +
                "Ну что, готова выйти из клетки, стать собой, обрести свободу и проявить свои стремления в жизни? \n" +
                "Жми \"Да\"");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        InlineKeyboardButton button = new InlineKeyboardButton();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        button.setText("Да!");
        button.setCallbackData("/new_payment");
        row1.add(button);
        buttons.add(row1);
        markup.setKeyboard((buttons));
        message2.setReplyMarkup(markup);
        sendMessage(message2);
    }

    private void addPayment(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Бот \"Твоя гармония\" + плюс канал работает в формате подписки\uD83E\uDE84\n" +
                "\n" +
                "Стоимость – всего 1200 рублей в месяц .\n" +
                "\n" +
                "Можно сразу приобрести подписку на несколько месяцев:\n" +
                "\n" +
                "⭐️3 месяца – 3600 р. \n" +
                "⭐️6 месяцев – 7200 р. \n" +
                "⭐️12 месяцев – 14400 р.\n" +
                "\n" +
                "Действует скидка 10% со 2-ого месяца за одного приглашенного друга, который приобретет подписку\uD83C\uDF81\n" +
                "\n" +
                "Для получение скидки напиши @Margo_Diaz имя друга, его ник в телеграм, день приобретения подписки. \n" +
                "Данные проверяются. \n" +
                "\n" +
                "Если у тебя не получается оплатить подписку или остались вопросы напиши @Margo_Diaz\n" +
                "\n" +
                "?Чтобы получить доступ в канал, нажимай на кнопку \"оплатить подписку\"");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        InlineKeyboardButton button = new InlineKeyboardButton();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        button.setText("Оплатить подписку");
        button.setCallbackData("/to_pay");
        row1.add(button);
        buttons.add(row1);
        markup.setKeyboard((buttons));
        message.setReplyMarkup(markup);
        sendMessage(message);

    }

    private void payload(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Здесь пока пусто, позже появится возможность оплаты. Пока можно перейти к следующим разделам:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Начать диалог");
        button.setCallbackData("/talk");
        row1.add(button);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("О Марго");
        button1.setCallbackData("/aboutMargo");
        row2.add(button1);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("Продукты");
        button3.setCallbackData("/products");
        row3.add(button3);


        buttons.add(row1);
        buttons.add(row2);
        buttons.add(row3);
        markup.setKeyboard(buttons);

        message.setReplyMarkup(markup);
        sendMessage(message);

    }

    private void talk(String chatId) {
        if (!users.containsKey(chatId)) {
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Вы ещё не оплатили подписку. Хотите начать тестовый период?");
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

            List<InlineKeyboardButton> row1 = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Начать тестовый период");
            button.setCallbackData("/test");
            row1.add(button);

            List<InlineKeyboardButton> row2 = new ArrayList<>();
            InlineKeyboardButton button1 = new InlineKeyboardButton();
            button1.setText("Оплатить подписку");
            button1.setCallbackData("/to_pay");
            row2.add(button1);

            buttons.add(row1);
            buttons.add(row2);
            markup.setKeyboard(buttons);

            message.setReplyMarkup(markup);
            sendMessage(message);
        }
    }

    private void test(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Техническое сообщение: Сейчас бот запущен в тестовом режиме, поэтому" +
                "информация о том, что для пользователя оформлен тестовый период не сохраняется. Сообщения далее " +
                "получили бы только те пользователи, у которых сейчас активен тестовый период.");
        sendMessage(message);
        if (testUsers.containsKey(chatId)) {
            if (testUsers.get(chatId) >= 0) {
                //здесь логика, которая будет перемещена позже
            }
        }
        SendMessage message1 = new SendMessage();
        message1.setChatId(chatId);
        message1.setText("Супер! \n" +
                "Привет. Я бот «Твоя гармония»\uD83E\uDD70\n" +
                "\n" +
                "В тестовом режиме у меня нет уникального аватара и имени. Но ты сможешь его выбрать после приобретения. " +
                "Я так рад с тобой пообщаться\uD83E\uDEF6\uD83C\uDFFB\n" +
                "\n" +
                "Расскажи, как твоё настроение? Пиши, пожалуйста, текстом, так я смогу понять тебя.");
        sendMessage(message1);

        SendMessage message2 = new SendMessage();
        message2.setChatId(chatId);
        message2.setText("Техническое сообщение: для обработки ответа будет обращение к gpt. Пока бот может воспринять " +
                "только два варианта: всё хорошо и я расстроен/расстроена");
        sendMessage(message2);

    }

    private void areYouGood(String message, String chatId) {
        SendMessage message1 = new SendMessage();
        if (message.equals("хорошо")) {
            message1.setChatId(chatId);
            message1.setText("Отлично! Мы можем продолжить общаться с тобой.");

        } else if (message.equals("плохо")) {
            message1.setChatId(chatId);
            message1.setText("О, расскажи, что тебя огорчило?\uD83E\uDD7A");

        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Продолжить");
        button.setCallbackData("/rate");
        row1.add(button);
        buttons.add(row1);
        markup.setKeyboard(buttons);
        message1.setReplyMarkup(markup);
        sendMessage(message1);


        SendMessage message2 = new SendMessage();
        message2.setChatId(chatId);
        message2.setText("Техническое сообщение: для обработки ответа будет обращение к gpt.");
        sendMessage(message2);


    }

    private void rateYourMood(String chatId) {
        SendMessage message1 = new SendMessage();
        message1.setChatId(chatId);
        message1.setText("Оцени своё состояние по шкале смайлов: \n" +
                "\n" +
                "\uD83E\uDD70 - 100% \n" +
                "\uD83D\uDE0A – 90 % \n" +
                "\uD83D\uDE01 – 80 %\n" +
                "\uD83D\uDE09 - 70 %\n" +
                "\uD83D\uDE0C - 60 %\n" +
                "\uD83D\uDE42 - 50 % \n" +
                "\uD83D\uDE15 - 40 %\n" +
                "\uD83D\uDE12 - 30 % \n" +
                "\uD83D\uDE1E - 20 %\n" +
                "\uD83E\uDD7A - 10 %\n" +
                "\uD83D\uDE2D - 0 %\n" +
                "\n" +
                "Выбери и пришли соответствующий смайл или число.");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("\uD83E\uDD70");
        button.setCallbackData("/mood100");
        row1.add(button);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("\uD83D\uDE0A");
        button1.setCallbackData("/mood90");
        row2.add(button1);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("\uD83D\uDE01");
        button3.setCallbackData("/mood80");
        row3.add(button3);

        List<InlineKeyboardButton> row4 = new ArrayList<>();
        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button4.setText("\uD83D\uDE09");
        button4.setCallbackData("/mood70");
        row4.add(button4);

        List<InlineKeyboardButton> row5 = new ArrayList<>();
        InlineKeyboardButton button5 = new InlineKeyboardButton();
        button5.setText("\uD83D\uDE0C");
        button5.setCallbackData("/mood60");
        row5.add(button5);

        List<InlineKeyboardButton> row6 = new ArrayList<>();
        InlineKeyboardButton button6 = new InlineKeyboardButton();
        button6.setText("\uD83D\uDE42");
        button6.setCallbackData("/mood50");
        row6.add(button6);

        List<InlineKeyboardButton> row7 = new ArrayList<>();
        InlineKeyboardButton button7 = new InlineKeyboardButton();
        button7.setText("\uD83D\uDE15");
        button7.setCallbackData("/mood40");
        row7.add(button7);

        List<InlineKeyboardButton> row8 = new ArrayList<>();
        InlineKeyboardButton button8 = new InlineKeyboardButton();
        button8.setText("\uD83D\uDE12");
        button8.setCallbackData("/mood30");
        row8.add(button8);

        List<InlineKeyboardButton> row9 = new ArrayList<>();
        InlineKeyboardButton button9 = new InlineKeyboardButton();
        button9.setText("\uD83D\uDE1E");
        button9.setCallbackData("/mood20");
        row9.add(button9);

        List<InlineKeyboardButton> row10 = new ArrayList<>();
        InlineKeyboardButton button10 = new InlineKeyboardButton();
        button10.setText("\uD83E\uDD7A");
        button10.setCallbackData("/mood10");
        row10.add(button10);

        List<InlineKeyboardButton> row11 = new ArrayList<>();
        InlineKeyboardButton button11 = new InlineKeyboardButton();
        button11.setText("\uD83D\uDE2D");
        button11.setCallbackData("/mood0");
        row11.add(button11);

        buttons.add(row1);
        buttons.add(row2);
        buttons.add(row3);
        buttons.add(row4);
        buttons.add(row5);
        buttons.add(row6);
        buttons.add(row7);
        buttons.add(row8);
        buttons.add(row9);
        buttons.add(row10);
        buttons.add(row11);
        markup.setKeyboard(buttons);

        message1.setReplyMarkup(markup);
        sendMessage(message1);

    }

    private void whatToDo(String chatId) {
        SendMessage message1 = new SendMessage();
        message1.setChatId(chatId);
        message1.setText("\uD83C\uDF40\uD83C\uDF3FЧтобы ты хотела сейчас улучшить?\n" +
                "\n" +
                "1  Эмоциональное состояние \n" +
                "2  Снять напряжение \n" +
                "3  Переключить фокус внимания \n" +
                "4  Расслабиться, наполнится энергией \n" +
                "\n" +
                "Выбери соответствующую цифру и отправь мне\uD83E\uDEF6\uD83C\uDFFB\n" +
                "\n" +
                "Больше техник спроси у меня и я пришлю тебе техники под твой запрос. Ты можешь описать его мне, сказав, например: \"Пришли мне список техник или практик для устранения грусти\". И прочее. \n" +
                "\n" +
                "\uD83E\uDE84Техники не заменяют работу с квалифицированным специалистом. \n" +
                "Чтобы обратиться к специалисту напиши @Margo_Diaz");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("1");
        button.setCallbackData("/techToFeelBetter");
        row1.add(button);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("2");
        button1.setCallbackData("/techToReduceStress");
        row2.add(button1);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("3");
        button3.setCallbackData("/techToChangeFocus");
        row3.add(button3);

        List<InlineKeyboardButton> row4 = new ArrayList<>();
        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button4.setText("4");
        button4.setCallbackData("/techToRelax");
        row4.add(button4);

        buttons.add(row1);
        buttons.add(row2);
        buttons.add(row3);
        buttons.add(row4);

        markup.setKeyboard(buttons);
        message1.setReplyMarkup(markup);

        sendMessage(message1);
    }

    private void showTechniques(String chatId, String message) {
        SendMessage message1 = new SendMessage();
        String text;
        if (message.contains("toFeelBetter")) {
            text = "Техника для улучшения эмоционального состояния:";
        } else if (message.contains("reduceStress")) {
            text = "Техника для снижения уровня стресса:";
        } else if (message.contains("changeFocus")) {
            text = "Техника для смены фокуса внимания:";
        } else if (message.contains("relax")) {
            text = "Техника для расслабления и наполнения энергией:";
        } else {
            text = "Техническое сообщение: здесь было бы обращение к gpt";
        }
        text = text + "Техники пока нет, она будет добавлена позже";
        message1.setText(text);
        message1.setChatId(chatId);
        sendMessage(message1);

        SendMessage message2 = new SendMessage();
        message2.setText("Больше техник спроси у меня и я пришлю тебе техники под твой запрос. " +
                "Ты можешь описать его мне, сказав, например: " +
                "\"Пришли мне список техник или практик для устранения грусти\". И прочее. ");
        message2.setChatId(chatId);
        sendMessage(message2);

        LocalDate date = LocalDate.now();
        Locale langRu = new Locale("ru");
        DayOfWeek day = date.getDayOfWeek();
        String str = day.getDisplayName(TextStyle.FULL, langRu);

        if (str.equals("понедельник") || str.equals("среда") || str.equals("суббота")) {
            SendMessage message3 = new SendMessage();
            message3.setText("\uD83E\uDE84Техники не заменяют работу с квалифицированным специалистом. \n" +
                    "Чтобы обратиться к специалисту напиши @Margo_Diaz \nэто сообщение было получено, так как сегодня " +
                    str + ".");
            message3.setChatId(chatId);
            sendMessage(message3);
        }

        SendMessage message4 = new SendMessage();
        message4.setChatId(chatId);
        message4.setText("Обязательно делись впечатлениями после выполнения практики " +
                "и изменениями в твоём состоянии\uD83E\uDD17");

        sendMessage(message4);

        SendMessage message5 = new SendMessage();
        message5.setChatId(chatId);
        message5.setText("Техническое сообщение: сообщение увидит только человек, который ранее не получал подарок" +
                "\n\nДорогой друг, у меня есть для тебя подарок \uD83C\uDF81 – файл с глубокой техникой для определения ментальных барьеров, мешающих тебе двигаться к лучшему будущему и жить той жизнью, которой ты хочешь.\n" +
                "  \n" +
                "\uD83E\uDE84Техника \"Глубина якоря\" поможет:\n" +
                "\n" +
                "• выявить скрытый блок и его глубину \n" +
                "• количество блоков \n" +
                "• устранить каждый из них \n" +
                "• задышать полной грудью \n" +
                "• обрести свободу и психоэмоциональный подъём \n" +
                "\n" +
                "В файле подробно объясняется каждый шаг выполнение техники. \n" +
                "\n" +
                "Задать вопросы по технике ты сможешь лично Марго в канале после приобретения подписки\uD83E\uDD17" +
                "\n\nтут будет файл" );
        sendMessage(message5);

        SendMessage message6 = new SendMessage();
        message6.setText("Техническое сообщение: это увидят те, кто не оплатил подписку." +
                "\n\nГотова продолжить нашу дружбу и стать членом прекрасного комьюнити?\uD83D\uDE3B\n" +
                "Жми \"оплатить подписку\". \n" +
                "\n" +
                "Ты выберешь мой уникальный аватар, мы сможем общаться каждый день и улучшать твоё состояние✨");
        message6.setChatId(chatId);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        InlineKeyboardButton button = new InlineKeyboardButton();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        button.setText("Оплатить подписку");
        button.setCallbackData("/new_payment");
        row1.add(button);
        buttons.add(row1);
        markup.setKeyboard((buttons));
        message6.setReplyMarkup(markup);
        sendMessage(message6);

        SendMessage message7 = new SendMessage();
        message7.setChatId(chatId);
        message7.setText("Техническое сообщение: если после этого человек продолжает писать, но тестовый период истёк, " +
                "будет сообщение с предложением оплатить. После оплаты появится меню, оно вызвано далее");
        sendMessage(message7);
        printMenu(chatId);

    }

    private void aboutMargo(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Почему мне можно доверять:\n" +
                "\n" +
                "1✨Я получила образование в ведущем институте России  " +
                "Московском институте психоанализа.\n" +
                "\n" +
                "2✨Образование трансформационный коуч ICI, в программе \"психология изменений и " +
                "трансформационный коучинг\".\n" +
                "\n" +
                "3✨Я состаю в международной коучинговой ассоциации ICI,  " +
                "подтверждено зарубежным документов и личным профилем на их сайте. \n" +
                "\n" +
                "4✨Более 400 часов практик и супервизий.\n" +
                "\n" +
                "5✨Я такой же человек как и вы.\n" +
                "Я живу, я полна чувств, эмоций и разнообразия событий. Я прожила свои тяжёлые " +
                "времена жизни и смогла их проработать в себе. Моя жизнь изменилась на до и после. \n" +
                "\n" +
                "6✨Помимо коучинга, я изучаю психологию, нейробиологию, квантовую физиологию " +
                "и познаю эту жизнь через философию и глубину её смыслов и истин.");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        InlineKeyboardButton button = new InlineKeyboardButton();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        button.setText("Меню");
        button.setCallbackData("/menu");
        row1.add(button);
        buttons.add(row1);
        markup.setKeyboard((buttons));
        sendMessage(message);

        InlineKeyboardMarkup markup1 = new InlineKeyboardMarkup();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        List<List<InlineKeyboardButton>> buttons1 = new ArrayList<>();
        List<InlineKeyboardButton> row11 = new ArrayList<>();
        button1.setText("Оплатить подписку");
        button1.setCallbackData("/new_payment");
        row11.add(button1);
        buttons1.add(row11);
        markup1.setKeyboard((buttons1));


        SendMessage message1 = new SendMessage();
        message1.setChatId(chatId);
        message1.setText("Техническое сообщение: Это увидят те, у кого нет подписки: " +
                "\nГотова продолжить нашу дружбу и стать членом прекрасного комьюнити?\uD83D\uDE3B\n" +
                "Жми \"оплатить подписку\". \n" +
                "\n" +
                "Ты выберешь мой уникальный аватар, мы сможем общаться каждый день и улучшать твоё состояние✨");

        sendMessage(message1);

    }

    private void spin(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("«Волшебная рулетка»\uD83D\uDD2E\n" +
                "\n" +
                "Пришло время получить свой подарок из 5 возможных призов:\n" +
                "\n" +
                "1\uD83D\uDC8Eскидка на индивидуальную программу Марго 5% \n" +
                "\n" +
                "2\uD83D\uDC8Eчек-лист «Исцеление внутреннего пространства» \n" +
                "\n" +
                "3\uD83D\uDC8Eтерапевтический сеанс с Марго «Карта личности через призму города» \n" +
                "\n" +
                "4\uD83D\uDC8E коуч - сессия с Марго (45 минут)\n" +
                "\n" +
                "5\uD83D\uDC8Eпрактика \n" +
                "\n" +
                "Жми «Крутить» и забирай свой приз\uD83C\uDFC6");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        InlineKeyboardButton button = new InlineKeyboardButton();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        button.setText("Крутить");
        button.setCallbackData("/getPrize");
        row1.add(button);
        buttons.add(row1);
        markup.setKeyboard((buttons));
        sendMessage(message);

    }

    private void getPrize(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Техническое сообщение: рулетка будет реализована позже");
    }


}
