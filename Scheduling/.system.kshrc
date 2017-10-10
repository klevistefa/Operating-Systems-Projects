#
# CIMS ksh startup file
#

#
# Set hostname
#
HOST=$(/usr/bin/uname -n)

#
# PATH and MANPATH
#
PATH=~/bin:/usr/local/bin:/opt/bin:/opt/sfw/bin:/usr/sfw/bin:/usr/dt/bin:/usr/openwin/bin:/opt/SUNWspro/bin:/opt/java/bin:/usr/ccs/bin:/usr/bin:/bin:/usr/sbin:/usr/ucb:/opt/ncarg/bin:/usr/local/gnu/bin:/usr/X11R6/bin:/sbin

MANPATH=~/man:/usr/local/man:/opt/sfw/man:/usr/sfw/man:/usr/dt/man:/usr/openwin/man:/opt/SUNWspro/man:/usr/local/gnu/man:/opt/java/man:/usr/man
export PATH MANPATH

#
# Misc. environments
#
export XAPPLRESDIR=/usr/local/lib/X11/app-defaults
export JAVA_HOME=/opt/java

#
# HPC setup
#
if [[ -d /opt/SUNWhpc ]] ; then
    PATH=$PATH:/opt/SUNWhpc/bin
    MANPATH=$MANPATH:/opt/SUNWhpc/man
fi

#
# SGE settings
#
[[ -d /opt/gridware/sge ]] && . /opt/gridware/sge/default/common/settings.sh

#
# TecPlot
#
export TEC100HOME=/opt/tecplot/10
export TEC360HOME=/opt/tecplot/360
export TECHOME=/opt/tecplot/current
#export TLMHOST='@128.122.157.5'
export TLMHOST='@128.122.251.27'

#
# NCARG environments
#
export NCARG_ROOT=/opt/ncarg

#
# NNTP server
#
export NNTPSERVER=netnews

#
# Use the better stocked terminal library in /usr/local
#
: ${TERMINFO:-/usr/local/share/terminfo}

#
# Aliases
#
alias cp='cp -i'
alias mv='mv -i'
alias rm='rm -i'
alias h='history'

#
# Other settings
#
PS1="${USER}@${HOST%%.*}[${PWD##*/}]\$ "
umask 077
ulimit -c 0

#
# Show usage/quota in login shell
#
if [[ $0 = -* && -x /usr/local/bin/showusage ]]; then
    /usr/local/bin/showusage
fi

