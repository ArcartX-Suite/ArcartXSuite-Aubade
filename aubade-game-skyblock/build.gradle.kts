plugins {
    id("java")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly(files("../aubade-core/libs/axs-api.jar"))
}

tasks.jar {
    from(zipTree("../aubade-core/libs/axs-api.jar")) {
        include("xuanmo/arcartxsuite/api/aubade/**")
    }
    archiveBaseName.set("Aubade-Game-SkyBlock")
}
