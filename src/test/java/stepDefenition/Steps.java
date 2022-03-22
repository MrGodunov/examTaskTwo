package stepDefenition;

import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.Затем;
import io.cucumber.java.ru.И;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static Utils.Configuration.getFromProperties;
import static io.restassured.RestAssured.given;

public class Steps {

    public String lastEpisode;
    public String mortySpecies;
    public String mortyLocation;
    public String lastCharacter;
    public String lastCharacterSpecie;
    public String lastCharacterLocation;

    @Дано("^Информация по персонажу")
    public void getMortyInfo(String id) {

        Response response1 = given()
                .baseUri(getFromProperties("apiUrl"))
                .contentType(ContentType.JSON)
                .when()
                .get("character/" + id)
                .then()
                .extract().response();

        String infoMorty = response1.getBody().asString();
        JSONObject jsonMorty = new JSONObject(infoMorty);
        JSONArray episodesWithMorty = jsonMorty.getJSONArray("episode");
        int episodesCount = episodesWithMorty.length();
        lastEpisode = episodesWithMorty.getString(episodesCount - 1);
        mortySpecies = jsonMorty.getString("species");
        mortyLocation = jsonMorty.getJSONObject("location").getString("name");
    }

    @Затем("^Получить последний персонаж в эпизоде$")
    public void getLastCharacter() {
        Response response2 = given()
                .baseUri(getFromProperties("apiUrl"))
                .contentType(ContentType.JSON)
                .when()
                .get(lastEpisode)
                .then()
                .extract().response();

        String lastMortyEpisode = response2.getBody().asString();
        JSONObject jsonLastEpisode = new JSONObject(lastMortyEpisode);
        JSONArray charactersInLastEpisode = jsonLastEpisode.getJSONArray("characters");
        int charactersCount = charactersInLastEpisode.length();
        lastCharacter = charactersInLastEpisode.getString(charactersCount - 1);
    }

    @Затем("^Получить информацию о последнем персонаже в эпизоде$")
    public void getLastCharacterInfo() {
        Response response3 = given()
                .baseUri(getFromProperties("apiUrl"))
                .contentType(ContentType.JSON)
                .when()
                .get(lastCharacter)
                .then()
                .extract().response();

        String desiredCharacter = response3.getBody().asString();
        JSONObject jsonCharacter = new JSONObject(desiredCharacter);
        lastCharacterSpecie = jsonCharacter.getString("species");
        lastCharacterLocation = jsonCharacter.getJSONObject("location").getString("name");
    }

    @И("^Проверить совпадение рас персонажей$")
    public void speciesAssert() {
        Assertions.assertEquals(mortySpecies, lastCharacterSpecie, "Совпадает");
    }

    @И("^Проверить различие локаций персонажей$")
    public void locationsAssert() {
        Assertions.assertNotEquals(mortyLocation, lastCharacterLocation, "Не совпадает");
    }

    @Затем("^Отправка запроса не регрес и сравнение результатов$")
    public void test2() throws IOException {
        JSONObject requestBody = new JSONObject(new String(Files.readAllBytes(Paths.get("src/test/resources/json/test2.json"))));
        requestBody.put("name", "Tomato");
        requestBody.put("job", "Eat maket");

        Response response3 = given()
                .baseUri(getFromProperties("regresApi"))
                .contentType("application/json;charset=UTF-8")
                .log().all()
                .when()
                .body(requestBody.toString())
                .post("users")
                .then()
                .statusCode(201)
                .log().all()
                .extract().response();

        String userTomato = response3.getBody().asString();
        JSONObject json = new JSONObject(userTomato);
        Assertions.assertEquals(json.getString("name"), "Tomato");
        Assertions.assertEquals(json.getString("job"), "Eat maket");
    }

}




