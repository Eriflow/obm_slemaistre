#!/usr/bin/perl -w -T

#+-------------------------------------------------------------------------+
#|   Copyright (c) 1997-2009 OBM.org project members team                  |
#|                                                                         |
#|  This program is free software; you can redistribute it and/or          |
#|  modify it under the terms of the GNU General Public License            |
#|  as published by the Free Software Foundation; version 2                |
#|  of the License.                                                        |
#|                                                                         |
#|  This program is distributed in the hope that it will be useful,        |
#|  but WITHOUT ANY WARRANTY; without even the implied warranty of         |
#|  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          |
#|  GNU General Public License for more details.                           | 
#+-------------------------------------------------------------------------+
#|  http://www.obm.org                                                     |
#+-------------------------------------------------------------------------+


package testObmSatellite;

use OBM::Log::log;
@ISA = ('OBM::Log::log');

use strict;

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};

use Getopt::Long;
my %parameters;
my $return = GetOptions( \%parameters, 'smtpinconf', 'smtpoutconf', 'cyrusPartitionsAdd', 'cyrusPartitionsDel' );

if( !$return ) {
    %parameters = undef;
}

my $testObmSatellite = testObmSatellite->new();
exit $testObmSatellite->run(\%parameters);

$|=1;


sub new {
    my $class = shift;
    my $self = bless { }, $class;

    $self->_configureLog();

    return $self;
}


sub DESTROY {
    my $self = shift;
}


sub run {
    my $self = shift;
    my( $parameters ) = @_;

    if( !defined($parameters) ) {
        $parameters->{'help'} = 1;
    }

    # Get parameters
    $self->_log( 'Analyse des parametres du script', 3 );
    $self->getParameter( $parameters ) or return 1;

    require OBM::Tools::obmDbHandler;
    $self->{'dbHandler'} = OBM::Tools::obmDbHandler->instance();
    if( !defined($self->{'dbHandler'}) ) {
        $self->_log( 'connecteur a la base de donnee invalide', 3 );
        print STDERR 'connecteur a la base de donnee invalide'."\n";
        return 1;
    }

    while( my( $paramName, $paramValue ) = each(%{$parameters}) ) {
        SWITCH: {
            if( $paramName =~ /^smtpinconf$/i ) {
                if( $self->smtpInConf() ) {
                    $self->_log( 'probleme lors de la mise à jour des maps des SMTP entrants', 0 );
                    print STDERR 'probleme lors de la mise à jour des maps des SMTP entrants'."\n";
                }else {
                    $self->_log( 'Mise a jour des maps de SMTP entrants réussie', 0 );
                    print STDERR 'Mise a jour des maps de SMTP entrants réussie'."\n";
                }
                last SWITCH;
            }

            if( $paramName =~ /^cyruspartitionsadd$/i ) {
                if( $self->cyrusPartitionConf() ) {
                    $self->_log( 'probleme lors de la mise à jour des partitions cyrus', 0 );
                    print STDERR 'probleme lors de la mise à jour des partitions cyrus'."\n";
                }else {
                    $self->_log( 'Mise a jour des partition Cyrus réussi', 0 );
                    print STDERR 'Mise a jour des partition Cyrus réussi'."\n";
                }
                last SWITCH;
            }
        }
    }

    return 0;
}


# fonction de verification des parametres du script
sub getParameter {
    my $self = shift;
    my( $parameters ) = @_;

    my $goodParams = 0;
    my $helpParam = 0;
    while( my( $paramName, $paramValue ) = each(%{$parameters}) ) {
        SWITCH: {
            if( $paramName =~ /^smtpinconf$/i ) {
                $self->_log( 'Mise a jour des tables Postfix des serveurs SMTP-in', 2 );
                $goodParams++;
                last SWITCH;
            }

            if( $paramName =~ /^smtpoutconf$/i ) {
                $self->_log( 'Mise a jour des tables Postfix des serveurs SMTP-out', 2 );
                $goodParams++;
                last SWITCH;
            }

            if( $paramName =~ /^cyruspartitionsadd$/i ) {
                $self->_log( 'Mise a jour (ajout) des partitions Cyrus', 2 );
                $goodParams++;
                last SWITCH;
            }

            if( $paramName =~ /^cyruspartitionsdel$/i ) {
                $self->_log( 'Mise a jour (suppression) des partitions Cyrus', 2 );
                $goodParams++;
                last SWITCH;
            }

            if( $paramName eq 'help' ) {
                $self->_log( 'Affichage de l\'aide', 2 );
                $helpParam = 1;
                last SWITCH;
            }
        }
    }

    # Affichage de l'aide
    if( !$goodParams || $helpParam ) {
        print STDERR 'Vous devez indiquer au moins un des paramètres suivants :'."\n";
        print STDERR "\t".'smtpInConf: permet de régénérer les tables Postfix des serveurs SMTP-in'."\n";
        print STDERR "\t".'smtpOutConf: permet de régénérer les tables Postfix des serveurs SMTP-out'."\n";
        print STDERR "\t".'cyrusPartitionsAdd: permet d\'ajouter les partitions Cyrus manquantes - Provoque un redémarrage du/des services Cyrus !'."\n";
        print STDERR "\t".'cyrusPartitionsDel: permet de supprimer les partitions Cyrus non déclarées - Provoque un redémarrage du/des services Cyrus !'."\n\n";
        return 0;
    }

    return 1;
}


sub smtpInConf {
    my $self = shift;

    require OBM::Postfix::smtpInEngine;
    if( !($self->{'smtpInEngine'} = OBM::Postfix::smtpInEngine->new()) ) {
        $self->_log( 'echec de l\'initialisation du SMTP-in maps updater', 0 );
        return 1;
    }

    my $query = 'SELECT domain_id
                 FROM Domain
                 WHERE NOT domain_global';

    my $sth;
    my $dbHandler = $self->{'dbHandler'};
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'chargement des Ids de domaines depuis la BD impossible', 3 );
        return 1;
    }

    while( my $domainId = $sth->fetchrow_hashref() ) {
        $self->{'smtpInEngine'}->updateByDomainId( [$domainId->{'domain_id'}] );
    }

    if( $self->{'smtpInEngine'}->updateMaps() ) {
        return 1;
    }

    return 0;
}


sub cyrusPartitionConf {
    my $self = shift;

    require OBM::Cyrus::cyrusRemoteEngine;
    my $imapUpdater = OBM::Cyrus::cyrusRemoteEngine->instance();
    if( !defined($imapUpdater) ) {
        return 1;
    }

    my $query = 'SELECT host.host_id
                    FROM Host host
                    INNER JOIN ServiceProperty ON CAST(serviceproperty_value AS UNSIGNED)=host_id AND serviceproperty_service=\'mail\' AND serviceproperty_property=\'imap\'';

    my $sth;
    my $dbHandler = $self->{'dbHandler'};
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'chargement des Ids de erveur IMAP depuis la BD impossible', 3 );
        return 1;
    }

    my $errorCode = 0;
    while( my $imapSrvId = $sth->fetchrow_hashref() ) {
        require OBM::Cyrus::cyrusServer;
        if( $imapUpdater->addCyrusPartition( OBM::Cyrus::cyrusServer->new( $imapSrvId->{'host_id'} ) ) ) {
            $errorCode = 1;
        }
    }

    return $errorCode;
}
