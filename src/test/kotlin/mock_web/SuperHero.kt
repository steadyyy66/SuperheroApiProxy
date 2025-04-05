package mock_web

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

//每次被调用 heroId都会+1 ，用来模拟新增的数据
var heroId = 0

fun main() {

    var response = SuperHeroResponseObj(
        response = "success",
        results = listOf(
            getNewHero()
        )
    )

    // 启动 Web 服务并返回这个结构体
    embeddedServer(Netty, port = 18081) {
        install(ContentNegotiation) {
            json()
        }

        routing {
            get("/63058498ccbb32bb8ee6e1aaa721ba76/search/{name}") {

                //
                if (heroId % 2 == 0) {
                    val mutableResults = response.results?.toMutableList()
                    mutableResults?.add(getNewHero())
                    response.results = mutableResults
                }

                call.respond(response)

            }
        }
    }.start(wait = true)
}


fun getNewHero(): Hero {

    heroId++
    var h = Hero(
        id = heroId.toString(),
        name = "Iron Man",
        powerstats = PowerStats(
            intelligence = "100",
            strength = "85",
            speed = "75",
            durability = "85",
            power = "100",
            combat = "95"
        ),
        biography = Biography(
            fullName = "Tony Stark",
            alterEgos = "No alter egos found.",
            aliases = listOf("Shellhead", "Golden Avenger"),
            placeOfBirth = "Long Island, New York",
            firstAppearance = "Tales of Suspense #39",
            publisher = "Marvel Comics",
            alignment = "good"
        ),
        appearance = Appearance(
            gender = "Male",
            race = "Human",
            height = listOf("6'1", "185 cm"),
            weight = listOf("225 lb", "102 kg"),
            eyeColor = "Blue",
            hairColor = "Black"
        )
    )
    return h
}


@Serializable
data class SuperHeroResponseObj(
    val response: String,
    var results: List<Hero>?
)

@Serializable
data class Hero(
    var id: String,
    val name: String,
    val powerstats: PowerStats,
    val biography: Biography,
    val appearance: Appearance
)

@Serializable
data class PowerStats(
    val intelligence: String,
    val strength: String,
    val speed: String,
    val durability: String,
    val power: String,
    val combat: String
)

@Serializable
data class Biography(
    val fullName: String,
    val alterEgos: String,
    val aliases: List<String>,
    val placeOfBirth: String,
    val firstAppearance: String,
    val publisher: String,
    val alignment: String
)

@Serializable
data class Appearance(
    val gender: String,
    val race: String,
    val height: List<String>,
    val weight: List<String>,
    val eyeColor: String,
    val hairColor: String
)