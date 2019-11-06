FROM python:3.7-slim-buster
MAINTAINER lego@est.tech

ENV PMSHUSER pmsh
ENV APPDIR /opt/app/${PMSHUSER}

# turn on file based EELF logging
ENV PROD_LOGGING 1

WORKDIR ${APPDIR}

COPY ./pmsh_service ./bin/
COPY setup.py ./
COPY requirements.txt ./

    # add non root user & group
RUN addgroup --system ${PMSHUSER} && adduser --ingroup ${PMSHUSER} --system ${PMSHUSER} && \
    # create logs dir and change permissions
    mkdir -p /var/log/ONAP/${PMSHUSER}/logs && \
    chmod a+w /var/log/ONAP/${PMSHUSER}/logs && \
    chown -R ${PMSHUSER}:${PMSHUSER} ${APPDIR} && \
    chmod 500 ${APPDIR}/bin/*.py && \
    chmod 500 ${APPDIR}/bin/*.sh && \
    # run the pip install
    pip install --upgrade pip && \
    pip install -r requirements.txt && \
    pip install -e .

# set the non root user
USER $PMSHUSER

# run the application
CMD ["./bin/pmsh_service.sh"]
