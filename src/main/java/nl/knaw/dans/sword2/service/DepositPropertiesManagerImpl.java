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
package nl.knaw.dans.sword2.service;

import nl.knaw.dans.sword2.Deposit;
import nl.knaw.dans.sword2.DepositState;
import nl.knaw.dans.sword2.config.Sword2Config;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

import javax.inject.Singleton;
import java.nio.file.Path;
import java.time.OffsetDateTime;

@Singleton
public class DepositPropertiesManagerImpl implements DepositPropertiesManager {
    private final String FILENAME = "deposit.properties";
    private final Sword2Config sword2Config;

    public DepositPropertiesManagerImpl(Sword2Config sword2Config) {
        this.sword2Config = sword2Config;
    }

    private Path getDepositPath(Path path) {
        return path.resolve(FILENAME);
    }

    @Override
    public void saveProperties(Path path, Deposit deposit) {
        var propertiesFile = getDepositPath(path);

        var params = new Parameters();
        var paramConfig = params.properties()
            .setFileName(propertiesFile.toString());

        var builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class, null, true).configure(
            paramConfig);

        try {
            var config = builder.getConfiguration();
            mapToConfig(config, deposit);
            builder.save();
        }
        catch (ConfigurationException cex) {
            // loading of the configuration file failed
            cex.printStackTrace();
        }
    }


    @Override
    public DepositProperties getProperties(Path path, Deposit deposit) {
        var propertiesFile = getDepositPath(path);

        var params = new Parameters();
        var paramConfig = params.properties()
            .setFileName(propertiesFile.toString());

        var builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class, null, true).configure(
            paramConfig);

        try {
            var config = builder.getConfiguration();
            return mapToDepositProperties(config);
        }
        catch (ConfigurationException cex) {
            // loading of the configuration file failed
            cex.printStackTrace();
        }

        return null;
    }

    @Override
    public Deposit getProperties(Path path) {

        var propertiesFile = getDepositPath(path);
        var params = new Parameters();
        var paramConfig = params.properties()
            .setFileName(propertiesFile.toString());

        var builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class, null, true).configure(
            paramConfig);

        try {
            var config = builder.getConfiguration();
            return mapToDeposit(config);
        }
        catch (ConfigurationException cex) {
            // loading of the configuration file failed
            cex.printStackTrace();
        }

        return null;
    }

    @Override
    public void saveProperties(Path path, Deposit deposit, DepositProperties properties) {
        // TODO implement logic for archived (see easy-sword2)
        var propertiesFile = getDepositPath(path);

        var params = new Parameters();
        var paramConfig = params.properties()
            .setFileName(propertiesFile.toString());

        var builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class, null, true).configure(
            paramConfig);

        try {
            var config = builder.getConfiguration();
            mapToConfig(config, properties);
            builder.save();
        }
        catch (ConfigurationException cex) {
            // loading of the configuration file failed
            cex.printStackTrace();
        }

    }


    DepositProperties mapToDepositProperties(Configuration config) {
        var depositProperties = new DepositProperties();
        depositProperties.setBagStoreBagId(config.getString("bag-store.bag-id"));
        depositProperties.setDataverseBagId(config.getString("dataverse.bag-id"));
        depositProperties.setCreationTimestamp(config.getString("creation.timestamp"));
        depositProperties.setDepositOrigin(config.getString("deposit.origin"));
        depositProperties.setDepositorUserId(config.getString("depositor.userId"));
        depositProperties.setStateDescription(config.getString("state.description"));
        depositProperties.setBagStoreBagName(config.getString("bag-store.bag-name"));
        depositProperties.setDataverseSwordToken(config.getString("dataverse.sword-token"));
        depositProperties.setContentType(config.getString("easy-sword2.client-message.content-type"));

        if (config.getString("state.label") != null) {
            depositProperties.setState(DepositState.valueOf(config.getString("state.label")));
        }

        return depositProperties;
    }

    Deposit mapToDeposit(Configuration config) {
        var deposit = new Deposit();
        deposit.setId(config.getString("bag-store.bag-id"));
        deposit.setCreated(OffsetDateTime.parse(config.getString("creation.timestamp")));
        deposit.setDepositor(config.getString("depositor.userId"));
        deposit.setState(DepositState.valueOf(config.getString("state.label")));
        deposit.setStateDescription(config.getString("state.description"));
        deposit.setBagName(config.getString("bag-store.bag-name"));
        deposit.setSwordToken(config.getString("dataverse.sword-token"));
        deposit.setMimeType(config.getString("easy-sword2.client-message.content-type"));

        return deposit;
    }

    void mapToConfig(Configuration config, DepositProperties depositProperties) {
        config.setProperty("bag-store.bag-id", depositProperties.getBagStoreBagId());
        config.setProperty("dataverse.bag-id", depositProperties.getDataverseBagId());
        config.setProperty("creation.timestamp", depositProperties.getCreationTimestamp());
        config.setProperty("deposit.origin", depositProperties.getDepositOrigin());
        config.setProperty("depositor.userId", depositProperties.getDepositorUserId());
        config.setProperty("state.label", depositProperties.getState()
            .toString());
        config.setProperty("state.description", depositProperties.getStateDescription());
        config.setProperty("bag-store.bag-name", depositProperties.getBagStoreBagName());
        config.setProperty("dataverse.sword-token", depositProperties.getDataverseSwordToken());

        if (depositProperties.getContentType() != null) {
            config.setProperty("easy-sword2.client-message.content-type", depositProperties.getContentType());
        } else {
            config.clearProperty("easy-sword2.client-message.content-type");
        }
    }


    void mapToConfig(Configuration config, Deposit deposit) {
        config.setProperty("bag-store.bag-id", deposit.getId());
        config.setProperty("dataverse.bag-id", String.format("urn:uuid:%s" ,deposit.getId()));
        config.setProperty("creation.timestamp", deposit.getCreated());
        config.setProperty("deposit.origin", "SWORD2");
        config.setProperty("depositor.userId", deposit.getDepositor());
        config.setProperty("state.label", deposit.getState().toString());
        config.setProperty("state.description", deposit.getStateDescription());
        config.setProperty("bag-store.bag-name", deposit.getBagName());
        config.setProperty("dataverse.sword-token", deposit.getSwordToken());

        if (deposit.getMimeType() != null) {
            config.setProperty("easy-sword2.client-message.content-type", deposit.getMimeType());
        } else {
            config.clearProperty("easy-sword2.client-message.content-type");
        }
    }
}
