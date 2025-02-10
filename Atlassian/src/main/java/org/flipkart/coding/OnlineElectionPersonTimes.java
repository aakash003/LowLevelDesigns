package org.flipkart.coding;

class TopVotedCandidate {
    int[] times;
    int[] leads;

    public TopVotedCandidate(int[] persons, int[] times) {
        int n = persons.length;
        this.times = times;
        leads = new int[n];

        int[] votes = new int[n];
        int maxVotes = 0, leader = -1;

        for (int i = 0; i < n; i++) {
            int p = persons[i];
            int t = times[i];

            votes[p]++;
            if (votes[p] >= maxVotes) {
                maxVotes = votes[p];
                leader = p;
            }

            leads[i] = leader;
        }
    }

    public int q(int t) {
        int lo = 0, hi = times.length - 1;
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            if (times[mid] <= t) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }
        return leads[lo];
    }

        public static void main(String[] args) {
            int[] persons = {0, 1, 1, 0, 0, 1, 0};
            int[] times = {0, 5, 10, 15, 20, 25, 30};

            TopVotedCandidate topVotedCandidate = new TopVotedCandidate(persons, times);

            System.out.println(topVotedCandidate.q(3));  // Output: 0
            System.out.println(topVotedCandidate.q(12)); // Output: 1
            System.out.println(topVotedCandidate.q(25)); // Output: 1
            System.out.println(topVotedCandidate.q(15)); // Output: 0
            System.out.println(topVotedCandidate.q(24)); // Output: 0
            System.out.println(topVotedCandidate.q(8));  // Output: 1
        }
}