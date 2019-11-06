FROM python:3.7-slim-buster
MAINTAINER lego@est.tech

ENV PMSHUSER pmsh
ENV APPDIR /opt/app/${PMSHUSER}

WORKDIR ${APPDIR}

COPY ./pmsh_service ./bin/
COPY setup.py ./
COPY requirements.txt ./

# run the install
RUN pip install --upgrade pip && \
    pip install -r requirements.txt && \
    pip install -e .

# it is an ONAP requirement to make, and switch to, a non root user
RUN addgroup --system $PMSHUSER && adduser --ingroup $PMSHUSER --system $PMSHUSER

# create logs dir and change permissions
RUN mkdir -p ${APPDIR}/logs && \
    chmod a+w ${APPDIR}/logs && \
    chown -R ${PMSHUSER}:${PMSHUSER} ${APPDIR} && \
    chmod 500 ${APPDIR}/bin/*.py && \
    chmod 500 ${APPDIR}/bin/*.sh

# turn on file based EELF logging
ENV PROD_LOGGING 1

USER $PMSHUSER

# Run the application
CMD ["./bin/pmsh_service.sh"]
