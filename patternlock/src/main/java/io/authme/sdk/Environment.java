package io.authme.sdk;

/*
   Copyright 2017 Authme ID Services Pvt. Ltd.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

import static io.authme.sdk.server.Config.PROD_SERVER_URL;
import static io.authme.sdk.server.Config.SANDBOX_SERVER_URL;

public enum Environment {
    SANDBOX("SANDBOX", SANDBOX_SERVER_URL),
    PRODUCTION("PRODUCTION", PROD_SERVER_URL);

    private String key;
    private String value;

    Environment(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
