Given an inputString(s) input source and target source, return the shipping price to send packages from one county to the next.
ex:
input = "US:UK:Fedex:5, CA:FR:DHL:10, FR:UK:UPS:6"
function costOfPackages(input, "US", "UK) -> 5
function costOfPackages(input, "FR", "UK) -> 6

Follow up Question: If given the input string, starting destination and ending destination, calculate the total shipping cost from the starting country to the ending

input = "US:UK:Fedex:5, CA:FR:DHL:10, FR:UK:UPS:6, UK:US:DHL:2, UK:FR:DHL:10"
function costOfPackages(input, "US", "FR") -> US to UK then UK to FR -> 15

It was essentially a variation of this:

At Stripe we keep track of where the money is and move money between bank accounts to make sure their balances are not below some threshold.
This is for operational and regulatory reasons, e.g. we should have enough funds to pay out to our users, and we are legally required to separate our users' funds from our own.
This interview question is a simplified version of a real-world problem we have here.
Let's say there are at most 500 bank accounts, some of their balances are above 100 and some are below.
How do you move money between them so that they all have at least 100?
Just to be clear we are not looking for the optimal solution, but a working one. *
Example input:
AU: 80
US: 140
MX: 110
SG: 120
FR: 70
Output:
from: US, to: AU, amount: 20
from: US, to: FR, amount: 20
from: MX, to: FR, amount: 10

Interview was ok, I solved the first one under 15 mins, the second one took a little more time because of how it was worded. I was at the end but ran out of time. I will say that Stripe focuses on the speed element as well. Be vocal but consise at the same time if that makes sense.

0

0