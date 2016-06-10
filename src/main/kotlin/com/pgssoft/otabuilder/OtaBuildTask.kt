/*
 * Copyright (C) 2016. PGS Software SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.pgssoft.otabuilder

import com.android.build.gradle.api.BaseVariantOutput
import com.android.builder.model.BuildType
import com.google.gson.GsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Created on 17/05/16.
 *
 *
 * @author bazted
 */
open class OtaBuildTask : DefaultTask() {

    val apkOutputsList = DefaultDomainObjectSet(BaseVariantOutput::class.java)
    val buildTypeList = DefaultDomainObjectSet(BuildType::class.java)

    val gson = GsonBuilder().create()

    private var startTime = 0L
    private var endTime = 0L

    var htmlDir: File? = null
    var flavourName: String = ""

    @TaskAction
    fun buildOta() {
        logger.lifecycle("greet from Kotlin OtaBuildTask")
        startTime = System.currentTimeMillis()

        val dir = htmlDir

        if (dir == null) {
            logger.lifecycle("html directory is null, please create directory")
            throw IllegalStateException("Please make sure that HTML directory was created")
        }

        if (!dir.exists() && dir.isDirectory) {
            dir.mkdirs()
            logger.lifecycle("created directory $dir")
        }

        val flavourBuildTypes = mutableListOf<FlavourData.BuildType>()

        apkOutputsList.forEach { output ->


            val type = buildTypeList.singleOrNull {
                type ->
                output.baseName.contains(type.name)
            } ?: throw IllegalStateException("No matching for type")

            val outputFile = output.outputFile
            if (!outputFile.exists()) {
                throw IllegalStateException("No apk file")
            }
            val apkFileName = outputFile.name

            val apkFile = File(dir, apkFileName)
            if (!apkFile.exists()) {
                apkFile.createNewFile()
                logger.lifecycle("creating $apkFile")
            }

            try {
                Files.copy(outputFile.toPath(), apkFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch(e: IOException) {
                throw IllegalStateException("Error while copying apk file")
            }

            val buildType = FlavourData.BuildType(type.name, apkFileName, output.versionCode, "test meta")
            flavourBuildTypes += buildType
        }

        val flavourData = FlavourData(flavourName, startTime, flavourBuildTypes)

        generateHtmlFiles(dir, flavourData)

        logger.lifecycle("build types ${buildTypeList.map { it.name }}")
        endTime = System.currentTimeMillis()

        logger.lifecycle("elapsed ${endTime - startTime}ms")

    }

    fun generateHtmlFiles(htmlDir: File, flavourData: FlavourData) {
        val indexResourcePath = "index.html"
        logger.lifecycle("indexPath= $indexResourcePath")

        val resource = javaClass.getResource(indexResourcePath)
                ?: throw NullPointerException("index resource is null")
        logger.lifecycle(resource.toString())
        val resourceStream = javaClass.getResourceAsStream(indexResourcePath)
                ?: throw IllegalStateException("cant find index.html resource")

        val targetIndexHtmlFile = File(htmlDir, "index.html")

        targetIndexHtmlFile.writeBytes(resourceStream.readBytes())

        val targetDataJsFile = File(htmlDir, "data.js")
        targetDataJsFile.writeText("var data = " + gson.toJson(flavourData))
    }
}