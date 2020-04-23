# ============LICENSE_START===================================================
#  Copyright (C) 2019-2020 Nordix Foundation.
# ============================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
# ============LICENSE_END=====================================================
import datetime
import logging as log
import sys
from logging.handlers import RotatingFileHandler
from os import makedirs

# These loggers will be overwritten with EELF logging when running in Docker
_AUDIT_LOGGER = log.getLogger("defaultlogger")
_ERROR_LOGGER = log.getLogger("defaultlogger")
_METRICS_LOGGER = log.getLogger("defaultlogger")

# Set up debug logger
_DEBUG_LOGGER = log.getLogger("defaultlogger")


def _create_logger(name, logfile):
    """
    Create a RotatingFileHandler and a streamhandler for stdout
    https://docs.python.org/3/library/logging.handlers.html
    what's with the non-pythonic naming in these stdlib methods? Shameful.
    """
    logger = log.getLogger(name)
    file_handler = RotatingFileHandler(logfile, maxBytes=10000000,  # 10 meg with one backup..,
                                       backupCount=2)
    formatter = log.Formatter("%(message)s")
    file_handler.setFormatter(formatter)
    logger.setLevel(log.DEBUG)
    stdout_handler = log.StreamHandler(sys.stdout)
    logger.addHandler(file_handler)
    logger.addHandler(stdout_handler)
    return logger


# Public

def get_module_logger(mod_name):
    """
    To use this, do logger = get_module_logger(__name__)
    """
    logger = log.getLogger(mod_name)
    handler = log.StreamHandler()
    formatter = log.Formatter("%(asctime)s "
                              "[%(name)-12s] "
                              "%(levelname)-8s "
                              "%(message)s")
    handler.setFormatter(formatter)
    logger.addHandler(handler)
    logger.setLevel(log.DEBUG)
    return logger


def create_loggers(logs_path=''):
    """
    Public method to set the global logger, launched from Run
    This is *not* launched during unit testing, so unit tests do not
    create/write log files
    """
    makedirs(logs_path, exist_ok=True)

    # create the audit log
    aud_file = logs_path + "/audit.log"
    open(aud_file, "a").close()  # this is like "touch"
    global _AUDIT_LOGGER
    _AUDIT_LOGGER = _create_logger("pmsh_service_audit", aud_file)

    # create the error log
    err_file = logs_path + "/error.log"
    open(err_file, "a").close()  # this is like "touch"
    global _ERROR_LOGGER
    _ERROR_LOGGER = _create_logger("pmsh_service_error", err_file)

    # create the metrics log
    met_file = logs_path + "/metrics.log"
    open(met_file, "a").close()  # this is like "touch"
    global _METRICS_LOGGER
    _METRICS_LOGGER = _create_logger("pmsh_service_metrics", met_file)

    # create the debug log
    debug_file = logs_path + "/debug.log"
    open(debug_file, "a").close()  # this is like "touch"
    global _DEBUG_LOGGER
    _DEBUG_LOGGER = _create_logger("pmsh_service_debug", debug_file)


def utc():
    """gets current time in utc"""
    return datetime.datetime.utcnow()


def debug(msg="n/a"):
    """
    This can be extended/modified to suit pmsh needs
    """
    ets = utc()

    _DEBUG_LOGGER.debug(
        "{ets}|{msg}".format(
            ets=ets.isoformat(),
            msg=msg,
        )
    )


"""
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
These loggers can be modified to suit the pmsh functionality
"""


def audit(raw_request, bts, xer, rcode, calling_mod, msg="n/a"):
    """
    write an EELF error record per
    'https://wiki.onap.org/download/attachments/1015849/
    ONAP%20application%20logging%20guidelines.pdf?api=v2'

    %The audit fields implemented:

    1 BeginTimestamp        Implemented (bts)
    2 EndTimestamp          Auto Injected when this is called
    3 RequestID             Implemented (xer)
    5 threadId              n/a
    7 serviceName           Implemented (from Req)
    9 StatusCode            Auto injected based on rcode
    10 ResponseCode         Implemented (rcode)
    13 Category log level - all audit records are INFO.
    15 Server IP address    Implemented (from Req)
    16 ElapsedTime          Auto Injected (milliseconds)
    17 Server               This is running in a Docker container so this is
                            not applicable, my HOSTNAME is always
                            "config_binding_service"
    18 ClientIPaddress      Implemented (from Req)
    19 class name           Implemented (mod), though docs say OOP,
                            I am using the python  module here
    20 Unused               ...implemented....
    21-25 Custom            n/a
    26 detailMessage        Implemented (msg)

    Not implemented
    4 serviceInstanceID - ?
    6 physical/virtual server name (Optional)
    8 PartnerName - nothing in the request tells me this
    11 Response Description - the CBS follows standard HTTP
                              error codes so look them up
    12 instanceUUID - Optional
    14 Severity (Optional)
    """
    ets = utc()

    _AUDIT_LOGGER.info(
        "{bts}|{ets}|{xer}||n/a||{path}||{status}|{rcode}|||INFO||{servip}|{et}|"
        "config_binding_service|{clientip}|{calling_mod}|||||||{msg}".format(
            bts=bts.isoformat(),
            ets=ets.isoformat(),
            xer=xer,
            rcode=rcode,
            path=raw_request.path.split("/")[1],
            status="COMPLETE" if rcode < 400 else "ERROR",
            servip=raw_request.host.split(":")[0],
            et=int((ets - bts).microseconds / 1000),
            clientip=raw_request.remote_addr,
            calling_mod=calling_mod,
            msg=msg,
        )
    )


def error(raw_request, xer, severity, ecode, tgt_entity="n/a",
          tgt_path="n/a", msg="n/a", adv_msg="n/a"):
    """
    write an EELF error record per
    'https://wiki.onap.org/download/attachments/1015849/
    ONAP%20application%20logging%20guidelines.pdf?api=v2'

    the error fields implemented:

    1 Timestamp          Auto Injected when this is called
    2 RequestID          Implemented (xer)
    3 ThreadID           n/a
    4 ServiceName        Implemented (from Req)
    6 TargetEntity       Implemented (tgt_entity)
    7 TargetServiceName Implemented (tgt_path)/
    8 ErrorCategory      Implemented (severity)
    9. ErrorCode         Implemented (ecode)
    10 ErrorDescription  Implemented (msg)
    11. detailMessage    Implemented (adv_msg)

    Not implemented:
    5 PartnerName - nothing in the request tells me this
    """
    ets = utc()

    _ERROR_LOGGER.error(
        "{ets}|{xer}|n/a|{path}||{tge}|{tgp}|{sev}|{ecode}|{msg}|{amsg}"
        .format(
            ets=ets,
            xer=xer,
            path=raw_request.path.split("/")[1],
            tge=tgt_entity,
            tgp=tgt_path,
            sev=severity,
            ecode=ecode,
            msg=msg,
            amsg=adv_msg,
        )
    )


def metrics(raw_request, bts, xer, target, target_path, rcode,
            calling_mod, msg="n/a"):
    """
    write an EELF error record per
    'https://wiki.onap.org/download/attachments/1015849/
    ONAP%20application%20logging%20guidelines.pdf?api=v2'

    %The metrics fields implemented:

    1 BeginTimestamp        Implemented (bts)
    2 EndTimestamp          Auto Injected when this is called
    3 RequestID             Implemented (xer)
    5 threadId              n/a
    7 serviceName           Implemented (from Req)
    9 TargetEntity          Implemented (target)
    10 TargetServiceName    Implemented (target_path)
    11 StatusCode           Implemented (based on rcode)
    12 Response Code        Implemented (rcode)
    15 Category log level   all metrics records are INFO.
    17 Server IP address    Implemented (from Req)
    18 ElapsedTime          Auto Injected (milliseconds)
    19 Server               This is running in a Docker container so this is
                            not applicable, my HOSTNAME is always
                            "config_binding_service"
    20 ClientIPaddress      Implemented (from Req)
    21 class name           Implemented (mod), though docs say OOP,
                            I am using the python  module here
    22 Unused               ...implemented....
    24 TargetVirtualEntity  n/a
    25-28 Custom            n/a
    29 detailMessage        Implemented (msg)

    Not implemented
    4 serviceInstanceID - ?
    6 physical/virtual server name (Optional)
    8 PartnerName - nothing in the request tells me this
    13 Response Description - the CBS follows standard HTTP error
                              codes so look them up
    14 instanceUUID - Optional
    16 Severity (Optional)
    23 ProcessKey - optional
    """
    ets = utc()

    _METRICS_LOGGER.info(
        "{bts}|{ets}|{xer}||n/a||{path}||{tge}|{tgp}|{status}|{rcode}|||INFO||{servip}|"
        "{et}|config_binding_service|{clientip}|{calling_mod}|||n/a|||||{msg}"
        .format(
            bts=bts.isoformat(),
            ets=ets.isoformat(),
            xer=xer,
            path=raw_request.path.split("/")[1],
            tge=target,
            tgp=target_path,
            status="COMPLETE" if rcode < 400 else "ERROR",
            rcode=rcode,
            servip=raw_request.host.split(":")[0],
            et=int((ets - bts).microseconds / 1000),
            clientip=raw_request.remote_addr,
            calling_mod=calling_mod,
            msg=msg,
        )
    )
