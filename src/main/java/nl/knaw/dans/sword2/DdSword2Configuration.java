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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.health.conf.HealthConfiguration;
import nl.knaw.dans.sword2.config.Sword2Config;
import nl.knaw.dans.sword2.config.UserConfig;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class DdSword2Configuration extends Configuration {

    @Valid
    private List<UserConfig> users;

    @Valid
    private Sword2Config sword2;

    @Valid
    @NotNull
    @JsonProperty("health")
    private HealthConfiguration healthConfiguration = new HealthConfiguration();

    public HealthConfiguration getHealthConfiguration() {
        return healthConfiguration;
    }

    public void setHealthConfiguration(final HealthConfiguration healthConfiguration) {
        this.healthConfiguration = healthConfiguration;
    }

    public Sword2Config getSword2() {
        return sword2;
    }

    public void setSword2(Sword2Config sword2) {
        this.sword2 = sword2;
    }

    public List<UserConfig> getUsers() {
        return users;
    }

    public void setUsers(List<UserConfig> users) {
        this.users = users;
    }
}
