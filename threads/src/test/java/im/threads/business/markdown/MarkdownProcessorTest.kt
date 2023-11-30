package im.threads.business.markdown

import android.content.Context
import androidx.core.text.toHtml
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MarkdownProcessorTest {
    private lateinit var markdownProcessor: MarkdownProcessor
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val messagesStyle = MarkdownConfig()

    private val firstSourceText = "[** * Gold Line of Los Angeles Metro * **](https://en.wikipedia.org/wiki/Gold_Line_(Los_Angeles_Metro)) includes stantions: \n" +
        "1. *Atlantic* \n" +
        "2. *East LA Civic Center* \n" +
        "3. Maravilla \n" +
        "4. *Indiana* \n" +
        "5. *Soto* \n" +
        "5. *Mariachi Plaza* \n" +
        "6. *Pico / Aliso* \n" +
        "7. *Little Tokyo / Arts District* \n" +
        "8. *Union Station* \n" +
        "9. *Chinatown* \n" +
        "10. *Lincoln/Cypress*"

    private val firstParsedText = "<p dir=\"ltr\"><a href=\"https://en.wikipedia.org/wiki/Gold_Line_(Los_Angeles_Metro)\">** * Gold Line of Los Angeles Metro * **</a> includes stantions:</p>\n" +
        "<p dir=\"ltr\">Atlantic</p>\n" +
        "<p dir=\"ltr\"><br>\n" +
        "</p>\n" +
        "<p dir=\"ltr\">East LA Civic Center</p>\n" +
        "<p dir=\"ltr\"><br>\n" +
        "</p>\n" +
        "<p dir=\"ltr\">Maravilla</p>\n" +
        "<p dir=\"ltr\"><br>\n" +
        "</p>\n" +
        "<p dir=\"ltr\">Indiana</p>\n" +
        "<p dir=\"ltr\"><br>\n" +
        "</p>\n" +
        "<p dir=\"ltr\">Soto</p>\n" +
        "<p dir=\"ltr\"><br>\n" +
        "</p>\n" +
        "<p dir=\"ltr\">Mariachi Plaza</p>\n" +
        "<p dir=\"ltr\"><br>\n" +
        "</p>\n" +
        "<p dir=\"ltr\">Pico / Aliso</p>\n" +
        "<p dir=\"ltr\"><br>\n" +
        "</p>\n" +
        "<p dir=\"ltr\">Little Tokyo / Arts District</p>\n" +
        "<p dir=\"ltr\"><br>\n" +
        "</p>\n" +
        "<p dir=\"ltr\">Union Station</p>\n" +
        "<p dir=\"ltr\"><br>\n" +
        "</p>\n" +
        "<p dir=\"ltr\">Chinatown</p>\n" +
        "<p dir=\"ltr\"><br>\n" +
        "</p>\n" +
        "<p dir=\"ltr\">Lincoln/Cypress</p>\n"

    private val secondSourceText = "Ставка по кредиту зависит от программы кредитования.   \n" +
        "\n" +
        " Более детально с программами кредитования вы можете ознакомиться на нашем сайте.   \n" +
        "[Беззалоговый кредит](https://eubank.kz/credits/cash-credit/)   \n" +
        "[Товарный кредит](https://eubank.kz/credits/product-credit/)   \n" +
        "[Зарплатный кредит](https://eubank.kz/credits/salary-credit/)   \n" +
        "[Автокредит](https://eubank.kz/autocredits/)   \n" +
        "[Кредит для лояльных клиентов](https://eubank.kz/credits/loyalty-credit/)   \n" +
        "[Внешнее рефинансирование](https://eubank.kz/credits/refinance/)   \n" +
        " [Ипотечный кредит](https://eubank.kz/mortgage/)   \n" +
        " [Мосметро. Пополнить баланс](mosmetro://main/lkp/pay)   \n" +
        " [СОГАЗ. Форма](sogaz://forms?code=dms&v=2)"

    private val secondParsedText = "<p dir=\"ltr\">&#1057;&#1090;&#1072;&#1074;&#1082;&#1072; &#1087;&#1086; &#1082;&#1088;&#1077;&#1076;&#1080;&#1090;&#1091; &#1079;&#1072;&#1074;&#1080;&#1089;&#1080;&#1090; &#1086;&#1090; &#1087;&#1088;&#1086;&#1075;&#1088;&#1072;&#1084;&#1084;&#1099; &#1082;&#1088;&#1077;&#1076;&#1080;&#1090;&#1086;&#1074;&#1072;&#1085;&#1080;&#1103;.</p>\n" +
        "<p dir=\"ltr\">&#1041;&#1086;&#1083;&#1077;&#1077; &#1076;&#1077;&#1090;&#1072;&#1083;&#1100;&#1085;&#1086; &#1089; &#1087;&#1088;&#1086;&#1075;&#1088;&#1072;&#1084;&#1084;&#1072;&#1084;&#1080; &#1082;&#1088;&#1077;&#1076;&#1080;&#1090;&#1086;&#1074;&#1072;&#1085;&#1080;&#1103; &#1074;&#1099; &#1084;&#1086;&#1078;&#1077;&#1090;&#1077; &#1086;&#1079;&#1085;&#1072;&#1082;&#1086;&#1084;&#1080;&#1090;&#1100;&#1089;&#1103; &#1085;&#1072; &#1085;&#1072;&#1096;&#1077;&#1084; &#1089;&#1072;&#1081;&#1090;&#1077;.<br>\n" +
        "<a href=\"https://eubank.kz/credits/cash-credit/\">&#1041;&#1077;&#1079;&#1079;&#1072;&#1083;&#1086;&#1075;&#1086;&#1074;&#1099;&#1081; &#1082;&#1088;&#1077;&#1076;&#1080;&#1090;</a><br>\n" +
        "<a href=\"https://eubank.kz/credits/product-credit/\">&#1058;&#1086;&#1074;&#1072;&#1088;&#1085;&#1099;&#1081; &#1082;&#1088;&#1077;&#1076;&#1080;&#1090;</a><br>\n" +
        "<a href=\"https://eubank.kz/credits/salary-credit/\">&#1047;&#1072;&#1088;&#1087;&#1083;&#1072;&#1090;&#1085;&#1099;&#1081; &#1082;&#1088;&#1077;&#1076;&#1080;&#1090;</a><br>\n" +
        "<a href=\"https://eubank.kz/autocredits/\">&#1040;&#1074;&#1090;&#1086;&#1082;&#1088;&#1077;&#1076;&#1080;&#1090;</a><br>\n" +
        "<a href=\"https://eubank.kz/credits/loyalty-credit/\">&#1050;&#1088;&#1077;&#1076;&#1080;&#1090; &#1076;&#1083;&#1103; &#1083;&#1086;&#1103;&#1083;&#1100;&#1085;&#1099;&#1093; &#1082;&#1083;&#1080;&#1077;&#1085;&#1090;&#1086;&#1074;</a><br>\n" +
        "<a href=\"https://eubank.kz/credits/refinance/\">&#1042;&#1085;&#1077;&#1096;&#1085;&#1077;&#1077; &#1088;&#1077;&#1092;&#1080;&#1085;&#1072;&#1085;&#1089;&#1080;&#1088;&#1086;&#1074;&#1072;&#1085;&#1080;&#1077;</a><br>\n" +
        "<a href=\"https://eubank.kz/mortgage/\">&#1048;&#1087;&#1086;&#1090;&#1077;&#1095;&#1085;&#1099;&#1081; &#1082;&#1088;&#1077;&#1076;&#1080;&#1090;</a><br>\n" +
        "<a href=\"mosmetro://main/lkp/pay\">&#1052;&#1086;&#1089;&#1084;&#1077;&#1090;&#1088;&#1086;. &#1055;&#1086;&#1087;&#1086;&#1083;&#1085;&#1080;&#1090;&#1100; &#1073;&#1072;&#1083;&#1072;&#1085;&#1089;</a><br>\n" +
        "<a href=\"sogaz://forms?code=dms&v=2\">&#1057;&#1054;&#1043;&#1040;&#1047;. &#1060;&#1086;&#1088;&#1084;&#1072;</a></p>\n"

    @Before
    fun before() {
        markdownProcessor = MarkwonMarkdownProcessor(context, messagesStyle, messagesStyle)
    }

    @Test
    fun givenFirstCase_whenOperatorFormattedMessage_thenParsedCorrectly() {
        val result = markdownProcessor.parseOperatorMessage(firstSourceText)
        assert(result.toHtml() == firstParsedText)
    }

    @Test
    fun givenSecondCase_whenOperatorFormattedMessage_thenParsedCorrectly() {
        val result = markdownProcessor.parseOperatorMessage(secondSourceText)
        assert(result.toHtml() == secondParsedText)
    }

    @Test
    fun givenFirstCase_whenClientFormattedMessage_thenParsedCorrectly() {
        val result = markdownProcessor.parseClientMessage(firstSourceText)
        assert(result.toHtml() == firstParsedText)
    }

    @Test
    fun givenSecondCase_whenClientFormattedMessage_thenParsedCorrectly() {
        val result = markdownProcessor.parseClientMessage(secondSourceText)
        assert(result.toHtml() == secondParsedText)
    }
}
