/*
 * Copyright (C) 2022 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.knaw.dans.sword2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import nl.knaw.dans.sword2.service.BagExtractorImpl;
import nl.knaw.dans.sword2.service.DepositManager;
import nl.knaw.dans.sword2.service.DepositManagerImpl;
import nl.knaw.dans.sword2.service.DepositPropertiesManager;
import nl.knaw.dans.sword2.service.DepositPropertiesManagerImpl;
import nl.knaw.dans.sword2.service.FileServiceImpl;
import nl.knaw.dans.sword2.service.ZipServiceImpl;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class DdSword2Configuration extends Configuration {

}
