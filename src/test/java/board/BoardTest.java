package board;

import base.BaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.*;

public class BoardTest extends BaseTest {


    private static Stream<Arguments> createNewBoardData() {
        return Stream.of(
                Arguments.of("1", 5),
                Arguments.of("My test board 5"),
                Arguments.of("000111222"));
    }

    @DisplayName("Create new board with valid name")
    @ParameterizedTest(name = "Valid board name: {0}")
    @MethodSource("createNewBoardData")
    public void createNewBoard(String boardName) {

        Response response = given()
                .spec(reqSpec)
                .queryParam("name", boardName)
                .when()
                .post(BASE_URL + "/" + BOARDS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath json = response.jsonPath();
        assertThat(json.getString("name")).isEqualTo(boardName);

        // Delete board
        String boardId = json.get("id");
        given()
                .spec(reqSpec)
                .when()
                .delete(BASE_URL + "/" + BOARDS + "/" + boardId)
                .then()
                .statusCode(200);
    }

    @Test
    public void createBoardWithEmptyBoardName() {

        Response response = given()
                .spec(reqSpec)
                .queryParam("name", "")
                .when()
                .post(BASE_URL + "/" + BOARDS)
                .then()
                .statusCode(400)
                .extract()
                .response();
    }

    @Test
    public void createBoardWithoutDefaultLists() {

        Response response = given()
                .spec(reqSpec)
                .queryParam("name", "Board without default lists")
                .queryParam("defaultLists", false)
                .when()
                .post(BASE_URL + "/" + BOARDS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath json = response.jsonPath();
        assertThat(json.getString("name")).isEqualTo("Board without default lists");

        String boardId = json.get("id");

        Response responseGet = given()
                .spec(reqSpec)
                .when()
                .get(BASE_URL + "/" + BOARDS + "/" + boardId + "/" + LISTS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath jsonGet = responseGet.jsonPath();
        List<String> idList = jsonGet.getList("id");
        assertThat(idList).hasSize(0);

        // Delete board
        given()
                .spec(reqSpec)
                .when()
                .delete(BASE_URL + "/" + BOARDS + "/" + boardId)
                .then()
                .statusCode(200);
    }

    @Test
    public void createBoardWithDefaultLists() {
        Response response = given()
                .spec(reqSpec)
                .queryParam("name", "Board with default lists")
                .queryParam("defaultLists", true)
                .when()
                .post(BASE_URL + "/" + BOARDS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath json = response.jsonPath();
        assertThat(json.getString("name")).isEqualTo("Board with default lists");

        String boardId = json.get("id");

        Response responseGet = given()
                .spec(reqSpec)
                .when()
                .get(BASE_URL + "/" + BOARDS + "/" + boardId + "/lists")
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath jsonGet = responseGet.jsonPath();
        List<String> nameList = jsonGet.getList("name");

        assertThat(nameList).hasSize(3).contains("To Do", "Doing", "Done");

        // Delete board
        given()
                .spec(reqSpec)
                .when()
                .delete(BASE_URL + "/" + BOARDS + "/" + boardId)
                .then()
                .statusCode(200);
    }

}
