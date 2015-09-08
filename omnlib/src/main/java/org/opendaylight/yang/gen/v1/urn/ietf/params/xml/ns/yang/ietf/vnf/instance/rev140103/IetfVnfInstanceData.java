package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.VNFInstance;


/**
 * This module contains a collection of YANG definitions for
 * managing VNF 
 * instance.
 * Copyright (c) 2013 IETF Trust and the persons identified as
 * authors 
 * of the code. All rights reserved.
 * Redistribution and use in source and binary 
 * forms, with or
 * without modification, is permitted pursuant to, and subject
 * to 
 * the license terms contained in, the Simplified BSD License
 * set forth in Section 
 * 4.c of the IETF Trust�s Legal Provisions
 * Relating to IETF 
 * Documents
 * (http://trustee.ietf.org/license-info).
 * <p>This class represents the following YANG schema fragment defined in module <b>ietf-vnf-instance</b>
 * <br />Source path: <i>META-INF/yang/ietf-vnf-instance.yang</i>):
 * <pre>
 * module ietf-vnf-instance {
 *     yang-version 1;
 *     namespace "urn:ietf:params:xml:ns:yang:ietf-vnf-instance";
 *     prefix "vnf";
 *     import ietf-inet-types { prefix "inet"; }
 *     revision 2014-01-03 {
 *         description "This module contains a collection of YANG definitions for
 *         managing VNF 
 *                     instance.
 *         
 *         Copyright (c) 2013 IETF Trust and the persons identified as
 *         authors 
 *                     of the code. All rights reserved.
 *         Redistribution and use in source and binary 
 *                     forms, with or
 *         without modification, is permitted pursuant to, and subject
 *         to 
 *                     the license terms contained in, the Simplified BSD License
 *         set forth in Section 
 *                     4.c of the IETF Trust�s Legal Provisions
 *         Relating to IETF 
 *                     Documents
 *         (http://trustee.ietf.org/license-info).
 *         ";
 *     }
 *     container VNF-instance {
 *         leaf id {
 *             type uint32;
 *         }
 *         leaf VNFD-name {
 *             type string;
 *         }
 *         container operation {
 *             leaf action {
 *                 type enumeration;
 *             }
 *             container parameter {
 *                 choice action {
 *                     case migration {
 *                         leaf destination-location {
 *                             type ip-address;
 *                         }
 *                     }
 *                     case scale {
 *                         leaf CPU-unit {
 *                             type uint16;
 *                         }
 *                         leaf memory-size {
 *                             type uint64;
 *                         }
 *                         leaf disk-size {
 *                             type uint64;
 *                         }
 *                     }
 *                 }
 *             }
 *         }
 *     }
 * }
 * </pre>
 */
public interface IetfVnfInstanceData
    extends
    DataRoot
{




    /**
     * VNF instance.
     */
    VNFInstance getVNFInstance();

}

