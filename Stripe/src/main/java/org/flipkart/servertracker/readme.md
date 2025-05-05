Server names consist of an alphabetic host type (e.g. "apibox") concatenated with the server number, with server numbers allocated as before (so "apibox1", "apibox2", etc. are valid hostnames).

Write a name tracking class with two operations, allocate(host_type) and deallocate(hostname). The former should reserve and return the next available hostname, while the latter should release that hostname back into the pool.

For example:

>> tracker = Tracker()

>> tracker.allocate("apibox")

"apibox1"

>> tracker.allocate("apibox")

"apibox2"

>> tracker.deallocate("apibox1")

nil

>> tracker.allocate("apibox")

"apibox1"

>> tracker.allocate("sitebox")

"sitebox1

>> tracker.allocate("#$@%")