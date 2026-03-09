import winrm
import argparse
import textwrap

def generate_powershell_script(domain):
    return textwrap.dedent(f"""
    $ErrorActionPreference = "Stop"
    
    $Groups = @("IT", "HR", "FIN", "CS", "RMC")
    $BaseNames = @("James", "Mary", "Robert", "Patricia", "John", "Jennifer", "Michael", "Linda", "David", "Elizabeth", "William", "Barbara", "Richard", "Susan", "Joseph", "Jessica", "Thomas", "Sarah", "Charles", "Karen", "Christopher", "Nancy", "Daniel", "Lisa", "Matthew", "Betty", "Anthony", "Margaret", "Mark", "Sandra", "Donald", "Ashley", "Steven", "Kimberly", "Paul", "Emily", "Andrew", "Donna", "Joshua", "Michelle", "Kenneth", "Dorothy", "Kevin", "Carol", "Brian", "Amanda", "George", "Melissa", "Edward", "Deborah", "Azman")

    Write-Host "Creating Active Directory Groups..."
    foreach ($GroupName in $Groups) {{
        if (-not (Get-ADGroup -Filter "Name -eq '$GroupName'" -ErrorAction SilentlyContinue)) {{
            New-ADGroup -Name $GroupName -GroupCategory Security -GroupScope Global
            Write-Host "Created group: $GroupName"
        }} else {{
            Write-Host "Group $GroupName already exists."
        }}
    }}

    Write-Host "Creating Users and adding to groups..."
    $random = New-Object System.Random
    
    foreach ($Group in $Groups) {{
        for ($i = 1; $i -le 10; $i++) {{
            # Randomly select a base name
            $BaseName = $BaseNames[$random.Next(0, $BaseNames.Count)]
            if ($i -eq 1 -and $Group -eq "IT") {{ $BaseName = "Azman" }} # Guarantee an IT_Azman
            
            $Username = "${{Group}}_${{BaseName}}"
            # Ensure uniqueness
            $Suffix = 1
            $FinalUsername = $Username
            while (Get-ADUser -Filter "SamAccountName -eq '$FinalUsername'" -ErrorAction SilentlyContinue) {{
                $FinalUsername = "${{Username}}${{Suffix}}"
                $Suffix++
            }}

            try {{
                $Password = ConvertTo-SecureString "Password123!" -AsPlainText -Force
                New-ADUser -Name $FinalUsername -SamAccountName $FinalUsername -UserPrincipalName "$FinalUsername@{domain}.local" -AccountPassword $Password -Enabled $true
                Add-ADGroupMember -Identity $Group -Members $FinalUsername
                Write-Host "Created user $FinalUsername and added to $Group"
            }} catch {{
                Write-Host "Failed to create user $FinalUsername : $_"
            }}
        }}
    }}

    $SharedRoot = "C:\\Shared"
    if (-not (Test-Path $SharedRoot)) {{
        New-Item -ItemType Directory -Path $SharedRoot | Out-Null
        Write-Host "Created root folder: $SharedRoot"
        
        # Share the root folder
        New-SmbShare -Name "Shared" -Path $SharedRoot -FullAccess "Everyone"
        Write-Host "Shared C:\\Shared as 'Shared'"
    }}

    Write-Host "Creating Department Folders and applying initial ACLs..."
    foreach ($Group in $Groups) {{
        $FolderPath = Join-Path $SharedRoot $Group
        if (-not (Test-Path $FolderPath)) {{
            New-Item -ItemType Directory -Path $FolderPath | Out-Null
        }}

        # Setup ACL: Disable inheritance, copy existing
        $Acl = Get-Acl $FolderPath
        $Acl.SetAccessRuleProtection($true, $true)
        
        # Grant the specific group Modify rights
        $AccessRule = New-Object System.Security.AccessControl.FileSystemAccessRule("{domain}\\$Group", "Modify", "ContainerInherit,ObjectInherit", "None", "Allow")
        $Acl.AddAccessRule($AccessRule)
        Set-Acl -Path $FolderPath -AclObject $Acl
        Write-Host "Configured folder and ACLs for $Group"
    }}

    Write-Host "Generating 10,000 random files and folders across department directories..."
    $totalEntities = 10000
    
    # Pre-create a pool of folder paths
    $ActiveFolders = @()
    foreach ($Group in $Groups) {{
        $ActiveFolders += Join-Path $SharedRoot $Group
    }}

    $FileExtensions = @(".txt", ".docx", ".xlsx", ".pdf", ".csv", ".log", ".dat", ".png", ".jpg", ".md")
    $Adjectives = @("Annual", "Quarterly", "Confidential", "Public", "Internal", "Draft", "Final", "Revised", "Archived", "Active", "Pending", "Approved", "Strategic", "Operational", "Global", "Regional", "Local", "Backup", "Secret", "Official")
    $Nouns = @("Report", "Strategy", "Budget", "Forecast", "Analysis", "Summary", "Proposal", "Plan", "Review", "Audit", "Meeting", "Minutes", "Agenda", "Schedule", "Memo", "Policy", "Procedure", "Manual", "Contract", "Presentation")

    # Generate directories first to create depth
    $targetFolderCount = [math]::Round($totalEntities * 0.1)
    for ($i = 0; $i -lt $targetFolderCount; $i++) {{ # 10% folders
        $ParentFolder = $ActiveFolders[$random.Next(0, $ActiveFolders.Count)]
        
        $Adj = $Adjectives[$random.Next(0, $Adjectives.Count)]
        $Noun = $Nouns[$random.Next(0, $Nouns.Count)]
        $RndNum = $random.Next(1000, 9999)
        $NewFolderName = "${{Adj}}_${{Noun}}_${{RndNum}}"
        
        $NewFolderPath = Join-Path $ParentFolder $NewFolderName
        
        # Windows MAX_PATH limitation safeguard
        while ($NewFolderPath.Length -gt 220) {{
            $ParentFolder = $ActiveFolders[$random.Next(0, $ActiveFolders.Count)]
            $NewFolderPath = Join-Path $ParentFolder $NewFolderName
        }}
        
        if (-not (Test-Path $NewFolderPath)) {{
            New-Item -ItemType Directory -Path $NewFolderPath | Out-Null
            $ActiveFolders += $NewFolderPath
            
            # Randomize Folder ACL
            $Acl = Get-Acl $NewFolderPath
            if ($random.Next(0, 10) -lt 3) {{
                $Acl.SetAccessRuleProtection($true, $true) # ~30% chance to break inheritance
            }}
            $RandomGrp = $Groups[$random.Next(0, $Groups.Count)]
            $RandomRights = @("Read", "Modify", "FullControl", "Write")[$random.Next(0, 4)]
            
            $TargetIdentity = "{domain}\\$RandomGrp"
            if ($random.Next(0, 2) -eq 1) {{
                $RandomUsr = "${{RandomGrp}}_" + $BaseNames[$random.Next(0, $BaseNames.Count)]
                $TargetIdentity = "{domain}\\$RandomUsr"
            }}
            
            try {{
                $Rule = New-Object System.Security.AccessControl.FileSystemAccessRule($TargetIdentity, $RandomRights, "ContainerInherit,ObjectInherit", "None", "Allow")
                $Acl.AddAccessRule($Rule)
                Set-Acl -Path $NewFolderPath -AclObject $Acl
            }} catch {{
                $FallbackIdentity = "{domain}\\$RandomGrp"
                $FallbackRule = New-Object System.Security.AccessControl.FileSystemAccessRule($FallbackIdentity, $RandomRights, "ContainerInherit,ObjectInherit", "None", "Allow")
                $Acl.AddAccessRule($FallbackRule)
                Set-Acl -Path $NewFolderPath -AclObject $Acl
            }}
        }}
    }}

    Write-Host "Created nested folders. Now generating files..."

    # Generate files in random paths
    $targetFileCount = $totalEntities - $targetFolderCount
    $batchSize = 250
    for ($i = 0; $i -lt $targetFileCount; $i += $batchSize) {{
        $amountToCreate = [math]::Min($batchSize, $targetFileCount - $i)
        
        for ($j = 0; $j -lt $amountToCreate; $j++) {{
            $TargetFolder = $ActiveFolders[$random.Next(0, $ActiveFolders.Count)]
            $Extension = $FileExtensions[$random.Next(0, $FileExtensions.Count)]
            
            $Adj = $Adjectives[$random.Next(0, $Adjectives.Count)]
            $Noun = $Nouns[$random.Next(0, $Nouns.Count)]
            $RndNum = $random.Next(1000, 9999)
            $FileName = "${{Adj}}_${{Noun}}_${{RndNum}}${{Extension}}"
            
            $FilePath = Join-Path $TargetFolder $FileName
            
            # Create a small dummy file natively
            [io.file]::WriteAllText($FilePath, "Confidential data for $TargetFolder generated at $(Get-Date)")

            # Randomize File ACL (Explicit Users or Groups)
            $FileAcl = Get-Acl $FilePath
            if ($random.Next(0, 10) -lt 3) {{
                $FileAcl.SetAccessRuleProtection($true, $true) # ~30% chance to break inheritance
            }}
            $RandomGrp = $Groups[$random.Next(0, $Groups.Count)]
            $RandomFileRights = @("Read", "Modify", "FullControl")[$random.Next(0, 3)]
            
            $TargetIdentity = "{domain}\\$RandomGrp"
            if ($random.Next(0, 2) -eq 1) {{
                $RandomUsr = "${{RandomGrp}}_" + $BaseNames[$random.Next(0, $BaseNames.Count)]
                $TargetIdentity = "{domain}\\$RandomUsr"
            }}
            
            try {{
                $FileRule = New-Object System.Security.AccessControl.FileSystemAccessRule($TargetIdentity, $RandomFileRights, "None", "None", "Allow")
                $FileAcl.AddAccessRule($FileRule)
                Set-Acl -Path $FilePath -AclObject $FileAcl
            }} catch {{
                $TargetIdentity = "{domain}\\$RandomGrp"
                $GrpRule = New-Object System.Security.AccessControl.FileSystemAccessRule($TargetIdentity, $RandomFileRights, "None", "None", "Allow")
                $FileAcl.AddAccessRule($GrpRule)
                Set-Acl -Path $FilePath -AclObject $FileAcl
            }}
        }}
        $progress = [math]::Round((($i + $amountToCreate) / $targetFileCount) * 100, 2)
        Write-Host "File generation progress: $progress%"
    }}

    Write-Host "Execution Complete."
    """)

def main():
    parser = argparse.ArgumentParser(description="Seed Windows Server via WinRM with AD Users, Groups, and a sprawling SMB structure.")
    parser.add_argument("--host", required=True, help="Windows Server IP or Hostname")
    parser.add_argument("--domain", required=False, default="DEV", help="Active Directory NetBIOS domain name (e.g. DEV)")
    parser.add_argument("--user", required=True, help="WinRM Admin Username (e.g. winrm_user or Administrator)")
    parser.add_argument("--password", required=True, help="WinRM Admin Password")
    parser.add_argument("--transport", default="ntlm", choices=["ntlm", "kerberos", "basic", "credssp"], help="WinRM transport protocol")
    parser.add_argument("--cert-validation", default="ignore", choices=["ignore", "validate"], help="Server Certificate Validation mode")

    args = parser.parse_args()

    print(f"Connecting to WinRM on {args.host} as {args.user}...")
    
    server_validation = 'ignore' if args.cert_validation == 'ignore' else 'validate'
    
    try:
        session = winrm.Session(
            args.host,
            auth=(args.user, args.password),
            transport=args.transport,
            server_cert_validation=server_validation
        )
        
        ps_script = generate_powershell_script(args.domain)
        
        print("Uploading script in chunks to bypass command line limits...")
        import base64
        # UTF-16LE is native to powershell base64, but we can do UTF8 since we decode it on their side
        b64_script = base64.b64encode(ps_script.encode('utf-8')).decode('utf-8')
        
        session.run_cmd(r'powershell -Command "if (Test-Path C:\Windows\Temp\seed.b64) { Remove-Item C:\Windows\Temp\seed.b64 -Force }"')
        
        chunk_size = 2000
        for i in range(0, len(b64_script), chunk_size):
            chunk = b64_script[i:i+chunk_size]
            session.run_cmd(f'cmd.exe /c echo {chunk}>> C:\\Windows\\Temp\\seed.b64')
            
        print("Executing remote PowerShell seeder script (This may take roughly 2-5 minutes depending on IOPS)...")
        
        decoder_ps = r"""
        $b64 = Get-Content C:\Windows\Temp\seed.b64 -Raw
        $b64 = $b64 -replace "`r", "" -replace "`n", "" -replace " ", ""
        $bytes = [System.Convert]::FromBase64String($b64)
        $script = [System.Text.Encoding]::UTF8.GetString($bytes)
        $script | Out-File C:\Windows\Temp\seed.ps1 -Encoding UTF8 -Force
        & C:\Windows\Temp\seed.ps1
        """
        result = session.run_ps(decoder_ps)

        # Output results
        if result.status_code == 0:
            print("\n[SUCCESS] PowerShell payload executed cleanly!")
            if result.std_out:
                print("--- Output ---")
                print(result.std_out.decode('utf-8'))
        else:
            print(f"\n[ERROR] PowerShell execution failed with exit code: {result.status_code}")
            if result.std_out:
                print("--- Standard Output ---")
                print(result.std_out.decode('utf-8'))
            if result.std_err:
                print("--- Error Output ---")
                print(result.std_err.decode('utf-8'))

    except Exception as e:
        print(f"\n[CRITICAL ERROR] Failed to connect or execute via WinRM: {e}")
        print("Hint: Ensure WinRM is enabled on the target server (Enable-PSRemoting -Force) and the execution policy allows running scripts.")

if __name__ == "__main__":
    main()
