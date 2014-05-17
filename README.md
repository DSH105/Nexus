Nexus
=====

DSH105's IRC bot. A versatile Java IRC bot using the PircBotX library.

According to Latin (`C17`), Nexus can be defined as `a binding together` (from `nectere` - `to bind`). Therefore, Nexus is the bot that brings everyone together.

I like contributions ;D

Current Features
================

We're very early in development, but we have the features below in place:

* Modular commands system
* Built-in help system
  * Users can PM the bot for info on all commands or to request extended information on a command.
* A few useful commands offering the following functionality:
  * A reminder command to ping a user (or yourself) after a set amount of time with a customisable message.
  * Currency converter
  * GitHub user/repo info retrieval
  * IP geolocation with google maps support
  * Channel statistics
  * Jenkins build info
    * Currently hardcoded to our CI server. We hope to make this configurable.
  * Temperature converter
  * Dogecoin/USD currency converter
* Some unit testing! We're still working on expanding coverage but the basics are in place.


Development Builds
==================

You can grab the latest builds from our [Jenkins instance](http://ci.hawkfalcon.com/view/DSH105/job/Nexus/).

![Build Status](https://api.travis-ci.org/repositories/DSH105/Nexus.svg)

JavaDoc
=======

JavaDocs are available on the [Jenkins instance](http://ci.hawkfalcon.com/view/DSH105/job/Nexus/javadoc/).
Contributing
============

See [here](CONTRIBUTING.md) for contributing guidelines.

You don't really need to worry about that though. The only thing I really want you to follow on that document is the PR formatting. 

We'll happily work with you on any PRs to ensure they can be pulled ASAP. :smile:

### Adding a new command

The most common contributions will be in the form of adding a new command. This is a relatively painless procedure. The general steps for doing so are outlined below:

1. Fork the repository and clone your fork
2. Navigate to the ``com.dsh105.nexus.command.module package`` in your IDE (in the ``src/main/java/com/dsh105/nexus/command/module/`` directory)
3. Choose the right category for your command, if not create one
4. Create a new class that extends the ``com.dsh105.nexus.command.CommandModule`` class
5. Add the [@Command](https://github.com/DSH105/Nexus/blob/master/src/main/java/com/dsh105/nexus/command/Command.java) (``com.dsh105.nexus.command.Command``) annotation to your class and implement the interface
6. Implement the **onCommand** method
  - In general returning true signifies a valid command (i.e. args were valid)
  - Returning false will result in a help message on command usage

You can take a look at the [channel stats command](https://github.com/DSH105/Nexus/blob/master/src/main/java/com/dsh105/nexus/command/module/general/ChannelStatsCommand.java) to give you a head-start.

Building
========

We use Maven 3 to build Nexus. Simply run the `mvn` command in the project root to compile the code and run the unit tests. You'll get a complete JAR in the target/ directory with the dependencies appropriately shaded for you.

Todo List
=========

* Allow disabling of certain commands per channel
* Custom command prefix for each channel
* More hooking into GitHub
  * Currently per-user authentication with GitHub is implemented (see `ghkey` command). Maybe there's a nicer way to do this instead of having to post Nexus the link?
* Plenty more commands
* Travis integration ([Click for more information](http://docs.travis-ci.com/api/))
