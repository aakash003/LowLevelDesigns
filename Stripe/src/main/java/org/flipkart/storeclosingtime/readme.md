Part-1
Customer log: "Y Y N Y" //represents if customer come to store every hour, Y means come, N means no.
Closing Time: Store is closed at a given hour
So need to compute the penalty by the rule:

if customers dont come (N) when the store is open: +1
if customers come (Y) when the store is close: +1
int compute_penalty(String log, int closing_time);
//scan input string from start to closing for 'N'; then closing to end for 'Y' to increment the penalty variable.
Part-2
Given customer log input string, find the closing time with minimum penalty.
int getClosingWithMinPenalty(String log);
// use the part 1 function to compute closing from 0 to end and keep track of minimum penalty

Part-3
Here the input string will be give for multiple stores like below format:
"BEGIN BEGIN BEGIN Y Y N Y END Y Y N N END Y N Y N END"
List getAllClosing(String log);
// use stack and part-2 function to get all closing time

**Part 1** — Compute Penalty for Given Closing Time

Part 2 — Find Closing Time with Minimum Penalty

Part 3 — Handle Multiple Stores in One Log