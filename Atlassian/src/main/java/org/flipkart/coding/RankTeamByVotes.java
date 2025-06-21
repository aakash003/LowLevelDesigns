package org.flipkart.coding;

import java.util.Arrays;

public class RankTeamByVotes {
    public String rankTeams(String[] votes) {
        int len = votes[0].length();
        int[][] map = new int[26][len + 1];
        for(int i = 0; i < 26; i++) map[i][len] = i;

        for(int i = 0; i < votes.length; i++){
            String s = votes[i];
            for(int j = 0; j < len; j++){
                map[s.charAt(j) - 'A'][j]++;
            }
        }
        Arrays.sort(map, (a, b) ->{
            for(int i = 0; i < len; i++){
                if(a[i] < b[i]) return 1;
                if(a[i] > b[i]) return -1;
            }
            return 0;
        });
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < len; i++){
            sb.append((char)('A' + map[i][len]));
        }
        return sb.toString();
    }
    public static void main(String[] args) {
        RankTeamByVotes ranker = new RankTeamByVotes();
        String[] votes = {"ABC", "ACB", "ABC", "ACB", "ACB"};
        String result = ranker.rankTeams(votes);
        System.out.println("Ranked Teams: " + result);  // Output: ACB
    }

}
