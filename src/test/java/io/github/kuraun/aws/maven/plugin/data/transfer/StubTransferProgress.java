/*
 * Copyright 2019-Present Kuraun Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.kuraun.aws.maven.plugin.data.transfer;

import io.github.kuraun.aws.maven.plugin.data.TransferProgress;

import java.util.Arrays;

public class StubTransferProgress implements TransferProgress {

    private volatile byte[] buffer;
    private volatile int length;

    @Override
    public void notify(byte[] buffer, int length) {
        this.buffer = Arrays.copyOf(buffer, buffer.length);
        this.length = length;
    }

    byte[] getBuffer() {
        return this.buffer;
    }

    int getLength() {
        return this.length;
    }

}
