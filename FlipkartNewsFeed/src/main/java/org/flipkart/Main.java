package org.flipkart;


import org.flipkart.entity.Post;
import org.flipkart.helper.ServiceFactory;
import org.flipkart.service.PlatformService;

import java.util.*;

public class Main {


    public static void main(String[] args) {
        PlatformService platformService = ServiceFactory.getPlatformService();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String inp = scanner.nextLine();
            inp = inp.trim();
            String[] inpArr = inp.split("~");
            try {
                switch (inpArr[0]) {
                    case "signup": {
                        platformService.signUp(inpArr[1]);
                    }
                    break;
                    case "login": {
                        platformService.login(inpArr[1]);
                    }
                    break;
                    case "post": {
                        platformService.post(inpArr[1]);
                    }
                    break;
                    case "logout": {
                        platformService.logout();
                    }
                    break;
                    case "follow": {
                        platformService.follow(inpArr[1]);
                    }
                    break;
                    case "comment": {
                        platformService.comment(inpArr[1], inpArr[2]);
                    }
                    break;
                    case "downvote": {
                        platformService.downvote(inpArr[1]);
                    }
                    break;
                    case "upvote": {
                        platformService.upvote(inpArr[1]);
                    }
                    break;
                    case "shownewsfeed": {
                        List<Post> postList = platformService.showNewsFeed();
                        for (Post post : postList) {
                            System.out.println("PostId=" + post.getPostId() + " " + "Content="+post.getContent() + " " + "Upvotes=" + post.getUpvotes() + " "+ "Downvotes="  + post.getDownvotes() + " " + "Author=" + post.getAuthor().getName() + " " + "Timestamp=" + post.getTimestamp() + " " + "Comments=" + post.getCommentListAsString());
                        }
                    }
                    break;
                    default:
                        System.out.println("Invalid Command");
                }
            } catch (RuntimeException runtimeException) {
                System.out.println(runtimeException);
            }
        }

    }


}