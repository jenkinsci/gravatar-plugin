# Jenkins Gravatar Plugin

[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/gravatar)](https://plugins.jenkins.io/gravatar)
[![Changelog](https://img.shields.io/github/v/tag/jenkinsci/gravatar-plugin?label=changelog)](https://github.com/jenkinsci/gravatar-plugin/blob/master/CHANGELOG.md)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/gravatar?color=blue)](https://plugins.jenkins.io/gravatar)
[![GitHub contributors](https://img.shields.io/github/contributors/jenkinsci/gravatar-plugin?color=blue)](https://github.com/jenkinsci/gravatar-plugin/graphs/contributors)

This plugins shows [Gravatar](http://gravatar.com/) avatars instead of
the generic user image.

![](docs/images/people-with-gravatar.png)

## Usage

Install the plugin on a Jenkins instance running at least v1.434. Thereafter, it
will automatically show Gravatars for the users who have an email and a Gravatar. No extra configuration needed except installing the plugin.

## Caveats

The plugin will re-check every 30 minutes to see if any user has
configured a Gravatar. Therefore, if you have configured a Gravatar and it does
not show up, please wait at least 30 minutes before thinking it is a bug.