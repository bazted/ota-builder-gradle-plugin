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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException

/**
 * Created on 17/05/16.
 *
 * Used https://github.com/KeepSafe/dexcount-gradle-plugin as base
 *
 * @author bazted
 *
 */
class OtaBuildPlugin implements Plugin<Project> {

    void apply(Project projectTarget) {
        if (projectTarget.plugins.hasPlugin('com.android.application')) {
            def appExtension = (AppExtension) projectTarget.android
            def variants = (DomainObjectCollection<ApplicationVariant>) appExtension.applicationVariants
            applyAndroid(projectTarget, variants)
        } else {
            throw new IllegalArgumentException('OtaBuilder plugin requires the Android plugin to be configured');
        }

    }

    private static void applyAndroid(Project project, DomainObjectCollection<ApplicationVariant> variants) {
        variants.all { variant ->

            println("=" + variant.name)
            def flavorName = variant.flavorName
            if (flavorName.empty) {
                println("we have only variant")
                createOrUpdateTask(variant.name, variant, project)

            } else {
                println("==" + flavorName)
                createOrUpdateTask(flavorName, variant, project)
            }
        }
    }

    private static void createOrUpdateTask(String name, ApplicationVariant variant, Project project) {
        def assembleTask = project.tasks["assemble"]

        def taskName = "generate${name.capitalize()}OTA"
        variant.outputs.each {
            variant.buildType
            println("===" + it.name)
            def task
            try {
                task = (OtaBuildTask) project.tasks.getByName(taskName)
            } catch (UnknownTaskException e) {
                println("created task for $taskName")
                task = project.tasks.create(taskName, OtaBuildTask)
                task.description = "Outputs OTA HTML for ${name}."
                task.group = 'OTA'
                task.flavourName = name

                def path = "${project.buildDir}/outputs/otaBuilder/${name}/"
                task.htmlDir = project.mkdir(path)
                task.dependsOn(assembleTask)
                task.mustRunAfter(assembleTask)
            }
            task.apkOutputsList.add(it)
            task.buildTypeList.add(variant.buildType)

            println("$taskName has ${task.apkOutputsList.size()} outputs")
        }
    }
}