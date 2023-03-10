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
package nl.knaw.dans.sword2.core.config;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.net.URL;
import java.util.List;

public class PasswordDelegateConfig {

    @Valid
    private URL url;
    @Valid
    @NotEmpty
    private List<String> forwardHeaders;

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public List<String> getForwardHeaders() {
        return forwardHeaders;
    }

    public void setForwardHeaders(List<String> forwardHeaders) {
        this.forwardHeaders = forwardHeaders;
    }

    @Override
    public String toString() {
        return "PasswordDelegateConfig{" +
            "passwordDelegate=" + url +
            ", forwardHeaders=" + forwardHeaders +
            '}';
    }
}
