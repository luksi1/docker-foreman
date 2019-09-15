class { 'r10k':
  sources => {
    'puppet' => {
      'remote'  => 'https://github.com',
      'basedir' => "${::settings::environmentpath}",
      'prefix'  => false,
    },
  },
  manage_modulepath => false
}

class { 'r10k::webhook':
  use_mcollective => false,
  user            => 'root',
  group           => '0',
  require         => Class['r10k::webhook::config'],
}

class {'r10k::webhook::config':
  use_mcollective  => false,
  public_key_path  => '/etc/mcollective/server_public.pem',  # Mandatory for FOSS
  private_key_path => '/etc/mcollective/server_private.pem', # Mandatory for FOSS
}
