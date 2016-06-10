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

/**
 * Created on 30/05/16.
 * data classes to generate data.js file for filling ota web-page
 *
 * @author bazted
 */
data class FlavourData(val name: String,
                       val buildDate: Long,
                       val buildTypes: List<BuildType>) {

    data class BuildType(val name: String,
                         val fileName: String,
                         val versionCode: Int,
                         val meta: String)
}

