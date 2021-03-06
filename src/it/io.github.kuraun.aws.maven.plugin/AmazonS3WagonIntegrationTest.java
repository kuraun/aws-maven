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

package io.github.kuraun.aws.maven.plugin;

import io.github.kuraun.aws.maven.plugin.data.TransferProgress;
import io.github.kuraun.aws.maven.plugin.data.transfer.StandardTransferListenerSupport;
import io.github.kuraun.aws.maven.plugin.data.transfer.StandardTransferProgress;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.resource.Resource;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.testutils.service.AwsTestBase;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

// WireMockRule is not still work.
public class AmazonS3WagonIntegrationTest extends AwsTestBase {

    // dummy file
    private static final String FILE_NAME = "robots.txt";

    /*
     * [profile aws-test-account] can access to bucket name.
     */
    private static final String BUCKET_NAME = System
            .getProperty("aws.maven.bucket");

    /*
     * [profile aws-test-account] can access to key(directory or repository) name.
     */
    private static final String BASE_DIRECTORY = "repo/";

    //    @Rule
    //    public WireMockRule mockServer = new WireMockRule(8443);

    private AmazonS3Wagon wagon;

    @Before
    public void setup() {
        //        S3Client s3Client = S3Client.builder().credentialsProvider(
        //                StaticCredentialsProvider
        //                        .create(AwsBasicCredentials.create("akid", "skid")))
        //                .region(Region.AP_NORTHEAST_1).endpointOverride(
        //                        URI.create("http://localhost:" + mockServer.port()))
        //                .serviceConfiguration(S3Configuration.builder()
        //                        .checksumValidationEnabled(false).build()).build();
        S3Client s3ClientIt = S3Client.builder().region(Region.AP_NORTHEAST_1)
                .credentialsProvider(CREDENTIALS_PROVIDER_CHAIN).build();

        wagon = new AmazonS3Wagon(s3ClientIt, BUCKET_NAME, BASE_DIRECTORY);
    }

    private TransferProgress getTransferProgress(int transferEvent) {
        return new StandardTransferProgress(new Resource("hoge"), transferEvent,
                new StandardTransferListenerSupport(wagon));
    }

    // Run only locally against own aws keys
    @Ignore
    @Test
    public void regionConnections() throws WagonException {
        AmazonS3Wagon remoteConnectingWagon = new AmazonS3Wagon();

        AuthenticationInfo authenticationInfo = new AuthenticationInfo();
        authenticationInfo.setUserName(System.getProperty("access.key"));
        authenticationInfo.setPassword(System.getProperty("secret.key"));

        Repository repository = new Repository("test",
                String.format("s3://%s/", "test"));
        remoteConnectingWagon
                .connectToRepository(repository, authenticationInfo, null);
        assertNotNull(remoteConnectingWagon.getFileList(""));
        remoteConnectingWagon.disconnectFromRepository();
    }

    @Test
    public void doesRemoteResourceExistExists() {
        //        stubFor(any(urlMatching(".*")).willReturn(
        //                aResponse().withStatus(200).withBody("<xml></xml>")));
        assertTrue(this.wagon.doesRemoteResourceExist(FILE_NAME));
    }

    @Test
    public void isRemoteResourceNewerNewer() throws Exception {
        //        stubFor(any(urlMatching(".*")).willReturn(
        //                aResponse().withStatus(200).withBody("<xml></xml>")));
        assertTrue(this.wagon.isRemoteResourceNewer(FILE_NAME, 0));
    }

    // WireMockRule not work
    //@Ignore
    @Test
    public void isRemoteResourceNewerOlder() throws ResourceDoesNotExistException {
        assertFalse(this.wagon.isRemoteResourceNewer(FILE_NAME, Long.MAX_VALUE));
    }

    // WireMockRule not work
    //@Ignore
    @Test
    public void isRemoteResourceNewerNoLastModified() throws ResourceDoesNotExistException {
        //        stubFor(any(urlMatching(".*")).willReturn(
        //                aResponse().withStatus(200).withBody("<xml></xml>")));
        assertTrue(this.wagon.isRemoteResourceNewer(FILE_NAME, 0));
    }

    // not work
    //@Ignore
    @Test(expected = ResourceDoesNotExistException.class)
    public void isRemoteResourceNewerDoesNotExist() throws ResourceDoesNotExistException {
        //        stubFor(any(urlMatching(".*")).willReturn(
        //                aResponse().withStatus(200).withBody("<xml></xml>")));
        this.wagon.isRemoteResourceNewer("frogs.txt", 0);
    }

    // WireMockRule not work
    //@Ignore
    @Test
    public void listDirectoryTopLevel() throws ResourceDoesNotExistException {
        //        stubFor(any(urlMatching(".*")).willReturn(
        //                aResponse().withStatus(200).withBody("<xml></xml>")));
        List<String> directoryContents = this.wagon.listDirectory("");
        assertTrue(directoryContents.contains(FILE_NAME));
        assertFalse(directoryContents.contains("frogs.txt"));
    }

    // WireMockRule not work
    //@Ignore
    @Test
    public void listDirectoryTopNested()
            throws TransferFailedException, ResourceDoesNotExistException {
        //        stubFor(any(urlMatching(".*")).willReturn(
        //                aResponse().withStatus(200).withBody("<xml></xml>")));

        File target = new File("src/test/resources/robots.txt");
        wagon.putResource(target, "release/robots.txt",
                getTransferProgress(TransferEvent.REQUEST_PUT));

        List<String> directoryContents = wagon.listDirectory("release/");

        assertTrue(directoryContents.contains(FILE_NAME));
        assertFalse(directoryContents.contains("frogs.txt"));
    }

    @Test(expected = ResourceDoesNotExistException.class)
    public void listDirectoryDoesNotExist() throws ResourceDoesNotExistException {
        //        stubFor(any(urlMatching(".*")).willReturn(
        //                aResponse().withStatus(200).withBody("<xml></xml>")));
        this.wagon.listDirectory("frogs");
    }

    @Test
    public void getResource()
            throws TransferFailedException, ResourceDoesNotExistException {
        //        stubFor(any(urlMatching(".*")).willReturn(
        //                aResponse().withStatus(200).withBody("<xml></xml>")));

        File target = new File("src/test/resources/robots.txt");
        this.wagon.putResource(target, FILE_NAME,
                getTransferProgress(TransferEvent.REQUEST_PUT));
        target.delete();
        assertFalse(target.exists());

        this.wagon.getResource(FILE_NAME, target,
                getTransferProgress(TransferEvent.REQUEST_GET));
        assertTrue(target.exists());
    }
}
