package org.example;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.*;

public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        int choice;

        Region region = Region.AP_NORTHEAST_2;
        Ec2Client ec2 = Ec2Client.builder()
                .region(region)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();

        while(true) {
            System.out.println("------------------------------------------------------------");
            System.out.println("  1. list   instance                2. available zones");
            System.out.println("  3. start  instance                4. available regions");
            System.out.println("  5. stop   instance                6. create instance");
            System.out.println("  7. reboot instance                8. list images");
            System.out.println("  9. terminate instance            10.");
            System.out.println("                                   99. quit");
            System.out.println("------------------------------------------------------------");
            System.out.print("Select Menu : ");
            choice = sc.nextInt();

            if (choice == 99) {
                System.out.println(" Stopping Service ");
                break;
            }
            else if (choice == 1) {
                ListingInstance(ec2);
            }
            else if (choice == 2){
                ShowAvailableZones(ec2);
            }
            else if (choice == 3){
                StartInstance(ec2);
            }
            else if (choice == 4){
                ShowAvailableRegions(ec2);
            }
            else if (choice == 5){
                StopInstance(ec2);
            }
            else if (choice == 6){
                CreateInstance(ec2);
            }
            else if (choice == 7){
                RebootInstance(ec2);
            }
            else if (choice == 8){
                ListingImages(ec2);
            }
            else if (choice == 9){
                TerminateInstance(ec2);
            }
            else {
                System.out.println(" Stopping Service ");
                break;
            }
        }

        sc.close();
        ec2.close();
    }

    public static void ListingInstance(Ec2Client ec2){
        String nextToken = null;
        try {
            do {
                DescribeInstancesRequest request = DescribeInstancesRequest.builder().maxResults(6).nextToken(nextToken).build();
                DescribeInstancesResponse response = ec2.describeInstances(request);
                for (Reservation reservation : response.reservations()) {
                    for (Instance instance : reservation.instances()) {
                        System.out.println("Instance Id is              : " + instance.instanceId());
                        System.out.println("Image Id is                 : " + instance.imageId());
                        System.out.println("Instance type is            : " + instance.instanceType());
                        System.out.println("Instance state name is      : " + instance.state().name());
                        System.out.println("Monitoring information is   : " + instance.monitoring().state());
                        System.out.println("");
                    }
                }
                System.out.println("[System] -- Wait 5 Second ---");
                try{
                    Thread.sleep(5000);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
                nextToken = response.nextToken();
            } while (nextToken != null);

        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorCode());
            System.exit(1);
        }
    }

    public static void ShowAvailableZones(Ec2Client ec2){
        try{
            DescribeAvailabilityZonesResponse response = ec2.describeAvailabilityZones();
            System.out.println("------------------------------------------------------------");
            for(AvailabilityZone zone : response.availabilityZones()){
                System.out.println("Available Zone : " + zone.zoneName());
                System.out.println("Zone Status    : " + zone.state());
                System.out.println("Zone Region    : " + zone.regionName());
                System.out.println("");
            }
            System.out.println("------------------------------------------------------------");
            System.out.println("[System] -- Wait 5 Second ---");
            try{
                Thread.sleep(5000);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
            System.out.println("");

        } catch (Ec2Exception e){
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    public static void StartInstance(Ec2Client ec2){
        String nextToken = null;
        int InstanceNumber = 1;
        int ChoiceInstance;
        Map<Integer, Object> StoppedInstanceList = new HashMap<>();
        Scanner scanner = new Scanner(System.in);

        System.out.println("------------------------------------------------------------");
        try {
            do {
                DescribeInstancesRequest request = DescribeInstancesRequest.builder().maxResults(6).nextToken(nextToken).build();
                DescribeInstancesResponse response = ec2.describeInstances(request);
                for (Reservation reservation : response.reservations()) {
                    for (Instance instance : reservation.instances()) {
                        if(instance.state().name().toString().equals("stopped")) {
                            System.out.println(" " + InstanceNumber + ". Instance(Stopped) Id is : " + instance.instanceId());
                            StoppedInstanceList.put(InstanceNumber, instance.instanceId());
                            InstanceNumber++;
                        }
                    }
                }
                nextToken = response.nextToken();
            } while (nextToken != null);

        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorCode());
            System.exit(1);
        }

        System.out.println("------------------------------------------------------------");
        System.out.print("Select Instance(Back : 99) : ");
        ChoiceInstance = scanner.nextInt();

        if(ChoiceInstance == 99){
            return;
        }
        else {
            StartInstancesRequest StartRequest = StartInstancesRequest.builder()
                    .instanceIds(StoppedInstanceList.get(ChoiceInstance).toString())
                    .build();

            ec2.startInstances(StartRequest);
            System.out.println("[SYSTEM] --- Start Instance Request Send Successfully ---");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void ShowAvailableRegions(Ec2Client ec2) {
        try {
            System.out.println("------------------------------------------------------------");
            DescribeRegionsResponse regionsResponse = ec2.describeRegions();
            for (software.amazon.awssdk.services.ec2.model.Region region : regionsResponse.regions()) {
                System.out.println("Found Region : " + region.regionName());
                System.out.println("Endpoint     : " + region.endpoint());
                System.out.println();
            }
            System.out.println("------------------------------------------------------------");
            System.out.println("[System] -- Wait 5 Second ---");
            try{
                Thread.sleep(5000);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    public static void StopInstance(Ec2Client ec2) {
        String nextToken = null;
        int InstanceNumber = 1;
        int ChoiceInstance;
        Map<Integer, Object> RunningInstanceList = new HashMap<>();
        Scanner scanner = new Scanner(System.in);

        System.out.println("------------------------------------------------------------");
        try {
            do {
                DescribeInstancesRequest request = DescribeInstancesRequest.builder().maxResults(6).nextToken(nextToken).build();
                DescribeInstancesResponse response = ec2.describeInstances(request);
                for (Reservation reservation : response.reservations()) {
                    for (Instance instance : reservation.instances()) {
                        if (instance.state().name().toString().equals("running")) {
                            System.out.println(" " + InstanceNumber + ". Instance(Running) Id is : " + instance.instanceId());
                            RunningInstanceList.put(InstanceNumber, instance.instanceId());
                            InstanceNumber++;
                        }
                    }
                }
                nextToken = response.nextToken();
            } while (nextToken != null);

        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorCode());
            System.exit(1);
        }

        System.out.println("------------------------------------------------------------");
        System.out.print("Select Instance(Back : 99) : ");
        ChoiceInstance = scanner.nextInt();

        if(ChoiceInstance == 99){
            return;
        } else {
            StopInstancesRequest StopRequest = StopInstancesRequest.builder()
                    .instanceIds(RunningInstanceList.get(ChoiceInstance).toString())
                    .build();

            ec2.stopInstances(StopRequest);
            System.out.println("[SYSTEM] --- Stop Instance Request Send Successfully ---");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void CreateInstance(Ec2Client ec2){
        Scanner scanner = new Scanner(System.in);
        String Name, ImageID, InstanceID;


        System.out.println("------------------------------------------------------------");
        System.out.print("[STEP - 1] Write Name : ");
        Name = scanner.nextLine();
        System.out.print("[STEP - 2] Write AMI Image ID(My Default - ami-09cf633fe86e51bf0)  : ");
        ImageID = scanner.nextLine();

        RunInstancesRequest request = RunInstancesRequest.builder()
                .imageId(ImageID)
                .instanceType(InstanceType.T2_MICRO)
                .maxCount(1)
                .minCount(1)
                .build();

        RunInstancesResponse response = ec2.runInstances(request);
        InstanceID = response.instances().get(0).instanceId();
        Tag tag = Tag.builder()
                .key("Name")
                .value(Name)
                .build();

        CreateTagsRequest tagsRequest = CreateTagsRequest.builder()
                .resources(InstanceID)
                .tags(tag)
                .build();

        try{
            ec2.createTags(tagsRequest);
            System.out.println("[SYSTEM] Started EC2 Instance : " + InstanceID);
        } catch (Ec2Exception e){
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    public static void RebootInstance(Ec2Client ec2){
        String nextToken = null;
        int InstanceNumber = 1;
        int ChoiceInstance;
        Map<Integer, Object> RunningInstanceList = new HashMap<>();
        Scanner scanner = new Scanner(System.in);

        System.out.println("------------------------------------------------------------");
        try {
            do {
                DescribeInstancesRequest request = DescribeInstancesRequest.builder().maxResults(6).nextToken(nextToken).build();
                DescribeInstancesResponse response = ec2.describeInstances(request);
                for (Reservation reservation : response.reservations()) {
                    for (Instance instance : reservation.instances()) {
                        if (instance.state().name().toString().equals("running")) {
                            System.out.println(" " + InstanceNumber + ". Instance(Running) Id is : " + instance.instanceId());
                            RunningInstanceList.put(InstanceNumber, instance.instanceId());
                            InstanceNumber++;
                        }
                    }
                }
                nextToken = response.nextToken();
            } while (nextToken != null);

        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorCode());
            System.exit(1);
        }

        System.out.println("------------------------------------------------------------");
        System.out.print("Select Instance(Back : 99) : ");
        ChoiceInstance = scanner.nextInt();

        if(ChoiceInstance == 99){
            return;
        } else {
            try {
                RebootInstancesRequest request = RebootInstancesRequest.builder()
                        .instanceIds(RunningInstanceList.get(ChoiceInstance).toString())
                        .build();

                ec2.rebootInstances(request);
                System.out.println("[SYSTEM] --- Reboot Instance Request Send Successfully ---");
            } catch (Ec2Exception e){
                System.err.println(e.awsErrorDetails().errorMessage());
                System.exit(1);
            }

        }
    }

    public static void ListingImages(Ec2Client ec2){
        DescribeImagesRequest request = DescribeImagesRequest.builder().owners("self").build();
        DescribeImagesResponse response = ec2.describeImages(request);
        List images = response.images();
        for(int i = 0; i < images.size(); i++){
            String[] str = images.get(i).toString().split(",");
            for(int j = 0; j < str.length; j++){
                if(str[j].contains("ImageId")) System.out.println("1 :" + str[j]);
            }
        }
    }

    public static void TerminateInstance(Ec2Client ec2){
        String nextToken = null;
        int InstanceNumber = 1;
        int ChoiceInstance;
        Map<Integer, Object> InstanceList = new HashMap<>();
        Scanner scanner = new Scanner(System.in);

        System.out.println("------------------------------------------------------------");
        try {
            do {
                DescribeInstancesRequest request = DescribeInstancesRequest.builder().maxResults(6).nextToken(nextToken).build();
                DescribeInstancesResponse response = ec2.describeInstances(request);
                for (Reservation reservation : response.reservations()) {
                    for (Instance instance : reservation.instances()) {
                        System.out.println(" " + InstanceNumber + ". Instance Id is : " + instance.instanceId());
                        InstanceList.put(InstanceNumber, instance.instanceId());
                        InstanceNumber++;
                    }
                }
                nextToken = response.nextToken();
            } while (nextToken != null);

        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorCode());
            System.exit(1);
        }

        System.out.println("------------------------------------------------------------");
        System.out.print("Select Instance(Back : 99) : ");
        ChoiceInstance = scanner.nextInt();

        if(ChoiceInstance == 99){
            return;
        } else {
            try {
                TerminateInstancesRequest request = TerminateInstancesRequest.builder()
                        .instanceIds(InstanceList.get(ChoiceInstance).toString())
                        .build();
                TerminateInstancesResponse response = ec2.terminateInstances(request);
                List<InstanceStateChange> list = response.terminatingInstances();
                for(InstanceStateChange stateChange : list) System.out.println("[SYSTEM] TERMINATED INSTANCE - " + stateChange.instanceId());
            } catch (Ec2Exception e){
                System.err.println(e.awsErrorDetails().errorMessage());
                System.exit(1);
            }

        }
    }
}