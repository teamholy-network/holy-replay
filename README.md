# HolyReplay

FORK FROM <a href="https://github.com/Jumper251/AdvancedReplay">AdvancedReplay</a>

AdvancedReplay is a Minecraft 1.8 & 1.21 Replay plugin. It can record players on your Server and save the recorded data to a file or database, so you can watch the replays at any time. Currently it records almost every action a player does and can be easily controlled with commands or the API.

## API
#### Add the dependency:
### Gradle
```text
repositories {
    maven {
        url = 'https://repo.gin1.cc/repository/releases/'
        credentials {
            username = System.getenv("HolyRepoUser") ?: HolyRepoUser
            password = System.getenv("HolyRepoPassword") ?: HolyRepoPassword
        }
    }
}

dependencies {
    compileOnly 'de.teamholy.replay:holyreplay:VERSION'
}
```

### API usage
Some examples on how to use the API can be found on the plugins Spigot page.
